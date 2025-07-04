package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_DETERMINATE_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_SENTENCED_UNDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.random.Random

class OffenderSentenceCreateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> = Stream.of(
      MandatoryFieldTestData("custodyType", createOrUpdateSentenceRequestBody(custodyType = "")),
      MandatoryFieldTestData("dateOfSentence", createOrUpdateSentenceRequestBody(dateOfSentence = "")),
      MandatoryFieldTestData("mappaLevel", createOrUpdateSentenceRequestBody(mappaLevel = "")),
    )
  }

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
  }

  @Test
  fun `given missing request body when create sentence called then bad request is returned`() {
    webTestClient.post()
      .uri(constructCreateSentenceUri(randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when create sentence called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val offenderId = randomPpudId()
    val errorFragment = data.errorFragment ?: data.propertyName
    postSentence(offenderId, data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing optional fields in request body when create offender called then 201 created is returned`() {
    val offenderId = createTestOffenderInPpud()
    val requestBodyWithOnlyMandatoryFields =
      """
        {
          "custodyType":"$PPUD_VALID_DETERMINATE_CUSTODY_TYPE",
          "dateOfSentence":"${randomDate()}",
          "mappaLevel":"$PPUD_VALID_MAPPA_LEVEL",
          "sentencedUnder":"$PPUD_VALID_SENTENCED_UNDER"
        }
      """.trimIndent()

    postSentence(offenderId, requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isCreated
  }

  @Test
  fun `given custody type is not determinate in request body when create sentence called then bad request is returned`() {
    // This is a temporary restriction until we handle indeterminate recalls
    val requestBody = createOrUpdateSentenceRequestBody(custodyType = randomString("custodyType"))
    postSentence(randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains("custodyType") })
  }

  @Test
  fun `given sentencingCourt is longer than 50 characters in request body when create sentence called then bad request is returned`() {
    val requestBody = createOrUpdateSentenceRequestBody(
      sentencingCourt = "A".repeat(51),
    )
    postSentence(randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains("sentencingCourt") })
  }

  @Test
  fun `given missing token when create sentence called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, constructCreateSentenceUri(randomPpudId()))
  }

  @Test
  fun `given token without recall role when create sentence called then forbidden is returned`() {
    val requestBody = createOrUpdateSentenceRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(constructCreateSentenceUri(randomPpudId()), requestBody)
  }

  @Test
  fun `given valid values in request body when create sentence called then sentence is created using supplied values`() {
    val offenderId = createTestOffenderInPpud()
    val dateOfSentence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val espCustodialPeriodYears = Random.nextInt(0, 1000)
    val espCustodialPeriodMonths = Random.nextInt(0, 1000)
    val espExtendedPeriodYears = Random.nextInt(0, 1000)
    val espExtendedPeriodMonths = Random.nextInt(0, 1000)
    val licenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentencingCourt = randomString("sentCourt")
    val sentencedUnder = PPUD_VALID_SENTENCED_UNDER
    val releaseDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceLengthPartYears = Random.nextInt(0, 1000)
    val sentenceLengthPartMonths = Random.nextInt(0, 1000)
    val sentenceLengthPartDays = Random.nextInt(0, 1000)
    val requestBody = createOrUpdateSentenceRequestBody(
      custodyType = PPUD_VALID_DETERMINATE_CUSTODY_TYPE,
      dateOfSentence = dateOfSentence,
      espCustodialPeriodYears = espCustodialPeriodYears,
      espCustodialPeriodMonths = espCustodialPeriodMonths,
      espExtendedPeriodYears = espExtendedPeriodYears,
      espExtendedPeriodMonths = espExtendedPeriodMonths,
      licenceExpiryDate = licenceExpiryDate,
      mappaLevel = PPUD_VALID_MAPPA_LEVEL_2,
      releaseDate = releaseDate,
      sentenceExpiryDate = sentenceExpiryDate,
      sentencingCourt = sentencingCourt,
      sentencedUnder = sentencedUnder,
      sentenceLengthPartYears = sentenceLengthPartYears,
      sentenceLengthPartMonths = sentenceLengthPartMonths,
      sentenceLengthPartDays = sentenceLengthPartDays,
    )

    val sentenceId = testPostSentence(offenderId, requestBody)

    val retrieved = retrieveOffender(offenderId)
    retrieved
      .jsonPath("offender.sentences.size()").isEqualTo(2)
      .jsonPath("offender.sentences[1].id").isEqualTo(sentenceId)
      .jsonPath("offender.sentences[1].custodyType").isEqualTo(PPUD_VALID_DETERMINATE_CUSTODY_TYPE)
      .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo(dateOfSentence)
      .jsonPath("offender.sentences[1].espCustodialPeriod.years").isEqualTo(espCustodialPeriodYears)
      .jsonPath("offender.sentences[1].espCustodialPeriod.months").isEqualTo(espCustodialPeriodMonths)
      .jsonPath("offender.sentences[1].espExtendedPeriod.years").isEqualTo(espExtendedPeriodYears)
      .jsonPath("offender.sentences[1].espExtendedPeriod.months").isEqualTo(espExtendedPeriodMonths)
      .jsonPath("offender.sentences[1].licenceExpiryDate").isEqualTo(licenceExpiryDate)
      .jsonPath("offender.sentences[1].mappaLevel").isEqualTo(PPUD_VALID_MAPPA_LEVEL_2)
      .jsonPath("offender.sentences[1].releaseDate").isEqualTo(releaseDate)
      .jsonPath("offender.sentences[1].sentencedUnder").isEqualTo(sentencedUnder)
      .jsonPath("offender.sentences[1].sentenceExpiryDate").isEqualTo(sentenceExpiryDate)
      .jsonPath("offender.sentences[1].sentenceLength.partYears").isEqualTo(sentenceLengthPartYears)
      .jsonPath("offender.sentences[1].sentenceLength.partMonths").isEqualTo(sentenceLengthPartMonths)
      .jsonPath("offender.sentences[1].sentenceLength.partDays").isEqualTo(sentenceLengthPartDays)
      .jsonPath("offender.sentences[1].sentencingCourt").isEqualTo(sentencingCourt)
  }

  @Test
  fun `given subsequent call with same values in request body when create sentence called then additional sentence is not created`() {
    val offenderId = createTestOffenderInPpud()
    val dateOfSentence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val espCustodialPeriodYears = Random.nextInt(0, 1000)
    val espCustodialPeriodMonths = Random.nextInt(0, 1000)
    val espExtendedPeriodYears = Random.nextInt(0, 1000)
    val espExtendedPeriodMonths = Random.nextInt(0, 1000)
    val licenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentencingCourt = randomString("sentCourt")
    val sentencedUnder = PPUD_VALID_SENTENCED_UNDER
    val releaseDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceLengthPartYears = Random.nextInt(0, 1000)
    val sentenceLengthPartMonths = Random.nextInt(0, 1000)
    val sentenceLengthPartDays = Random.nextInt(0, 1000)
    val requestBody = createOrUpdateSentenceRequestBody(
      custodyType = PPUD_VALID_DETERMINATE_CUSTODY_TYPE,
      dateOfSentence = dateOfSentence,
      espCustodialPeriodYears = espCustodialPeriodYears,
      espCustodialPeriodMonths = espCustodialPeriodMonths,
      espExtendedPeriodYears = espExtendedPeriodYears,
      espExtendedPeriodMonths = espExtendedPeriodMonths,
      licenceExpiryDate = licenceExpiryDate,
      mappaLevel = PPUD_VALID_MAPPA_LEVEL_2,
      releaseDate = releaseDate,
      sentenceExpiryDate = sentenceExpiryDate,
      sentencingCourt = sentencingCourt,
      sentencedUnder = sentencedUnder,
      sentenceLengthPartYears = sentenceLengthPartYears,
      sentenceLengthPartMonths = sentenceLengthPartMonths,
      sentenceLengthPartDays = sentenceLengthPartDays,
    )

    val firstCallSentenceId = testPostSentence(offenderId, requestBody)
    val secondCallSentenceId = testPostSentence(offenderId, requestBody)

    assertEquals(firstCallSentenceId, secondCallSentenceId)
    val retrieved = retrieveOffender(offenderId)
    retrieved
      .jsonPath("offender.sentences.size()").isEqualTo(2)
      .jsonPath("offender.sentences[1].id").isEqualTo(firstCallSentenceId)
      .jsonPath("offender.sentences[1].custodyType").isEqualTo(PPUD_VALID_DETERMINATE_CUSTODY_TYPE)
      .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo(dateOfSentence)
      .jsonPath("offender.sentences[1].espCustodialPeriod.years").isEqualTo(espCustodialPeriodYears)
      .jsonPath("offender.sentences[1].espCustodialPeriod.months").isEqualTo(espCustodialPeriodMonths)
      .jsonPath("offender.sentences[1].espExtendedPeriod.years").isEqualTo(espExtendedPeriodYears)
      .jsonPath("offender.sentences[1].espExtendedPeriod.months").isEqualTo(espExtendedPeriodMonths)
      .jsonPath("offender.sentences[1].licenceExpiryDate").isEqualTo(licenceExpiryDate)
      .jsonPath("offender.sentences[1].mappaLevel").isEqualTo(PPUD_VALID_MAPPA_LEVEL_2)
      .jsonPath("offender.sentences[1].releaseDate").isEqualTo(releaseDate)
      .jsonPath("offender.sentences[1].sentencedUnder").isEqualTo(sentencedUnder)
      .jsonPath("offender.sentences[1].sentenceExpiryDate").isEqualTo(sentenceExpiryDate)
      .jsonPath("offender.sentences[1].sentenceLength.partYears").isEqualTo(sentenceLengthPartYears)
      .jsonPath("offender.sentences[1].sentenceLength.partMonths").isEqualTo(sentenceLengthPartMonths)
      .jsonPath("offender.sentences[1].sentenceLength.partDays").isEqualTo(sentenceLengthPartDays)
      .jsonPath("offender.sentences[1].sentencingCourt").isEqualTo(sentencingCourt)
  }

  private fun testPostSentence(offenderId: String, requestBody: String): String {
    val idExtractor = ValueConsumer<String>()
    postSentence(offenderId, requestBody)
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("sentence.id").value(idExtractor)
    val id = idExtractor.value
    org.junit.jupiter.api.Assertions.assertNotNull(id, "ID returned from create sentence request is null")
    org.junit.jupiter.api.Assertions.assertTrue(id!!.isNotEmpty(), "ID returned from create sentence request is empty")
    return id
  }

  private fun postSentence(offenderId: String, requestBody: String): WebTestClient.ResponseSpec = webTestClient.post()
    .uri(constructCreateSentenceUri(offenderId))
    .headers { it.authToken() }
    .contentType(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(requestBody))
    .exchange()

  private fun constructCreateSentenceUri(offenderId: String) = "/offender/$offenderId/sentence"
}
