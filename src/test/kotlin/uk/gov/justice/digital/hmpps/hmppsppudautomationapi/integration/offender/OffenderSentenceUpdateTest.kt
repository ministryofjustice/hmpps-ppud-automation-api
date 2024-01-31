package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.random.Random

@ExtendWith(OffenderSentenceUpdateTest.DataTidyExtension::class)
class OffenderSentenceUpdateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("custodyType", createOrUpdateSentenceRequestBody(custodyType = "")),
      )
    }
  }

  internal class DataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing request body when update sentence called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUpdateSentenceUri(randomPpudId(), randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when update sentence called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val offenderId = randomPpudId()
    val sentenceId = randomPpudId()
    val errorFragment = data.errorFragment ?: data.propertyName
    putSentence(offenderId, sentenceId, data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing optional fields in request body when update offender called then 200 OK is returned`() {
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val requestBodyWithOnlyMandatoryFields = "{" +
      "\"custodyType\":\"$PPUD_VALID_CUSTODY_TYPE\", " +
      "\"dateOfSentence\":\"${randomDate()}\", " +
      "\"mappaLevel\":\"$PPUD_VALID_MAPPA_LEVEL\" " +
      "}"

    putSentence(offenderId, sentenceId, requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isOk
  }

  @Test
  fun `given custody type is not determinate in request body when update sentence called then bad request is returned`() {
    // This is a temporary restriction until we handle indeterminate recalls
    val requestBody = createOrUpdateSentenceRequestBody(custodyType = randomString("custodyType"))
    putSentence(randomPpudId(), randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains("custodyType") })
  }

  @Test
  fun `given missing token when update sentence called then unauthorized is returned`() {
    val uri = constructUpdateSentenceUri(randomPpudId(), randomPpudId())
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, uri)
  }

  @Test
  fun `given token without recall role when update sentence called then forbidden is returned`() {
    val requestBody = createOrUpdateSentenceRequestBody()
    val uri = constructUpdateSentenceUri(randomPpudId(), randomPpudId())
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(uri, requestBody, HttpMethod.PUT)
  }

  @Test
  fun `given valid values in request body when update sentence called then sentence is updated using supplied values`() {
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val dateOfSentence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val espCustodialPeriodYears = Random.nextInt(0, 1000)
    val espCustodialPeriodMonths = Random.nextInt(0, 1000)
    val espExtendedPeriodYears = Random.nextInt(0, 1000)
    val espExtendedPeriodMonths = Random.nextInt(0, 1000)
    val licenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentencingCourt = randomString("sentCourt")
    val releaseDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceExpiryDate = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val sentenceLengthPartYears = Random.nextInt(0, 1000)
    val sentenceLengthPartMonths = Random.nextInt(0, 1000)
    val sentenceLengthPartDays = Random.nextInt(0, 1000)
    val requestBody = createOrUpdateSentenceRequestBody(
      custodyType = PPUD_VALID_CUSTODY_TYPE,
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
      sentenceLengthPartYears = sentenceLengthPartYears,
      sentenceLengthPartMonths = sentenceLengthPartMonths,
      sentenceLengthPartDays = sentenceLengthPartDays,
    )

    testPutSentence(offenderId, sentenceId, requestBody)

    val retrieved = retrieveOffender(offenderId)
    retrieved
      .jsonPath("offender.sentences.size()").isEqualTo(1)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
      .jsonPath("offender.sentences[0].custodyType").isEqualTo(PPUD_VALID_CUSTODY_TYPE)
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo(dateOfSentence)
      .jsonPath("offender.sentences[0].espCustodialPeriod.years").isEqualTo(espCustodialPeriodYears)
      .jsonPath("offender.sentences[0].espCustodialPeriod.months").isEqualTo(espCustodialPeriodMonths)
      .jsonPath("offender.sentences[0].espExtendedPeriod.years").isEqualTo(espExtendedPeriodYears)
      .jsonPath("offender.sentences[0].espExtendedPeriod.months").isEqualTo(espExtendedPeriodMonths)
      .jsonPath("offender.sentences[0].licenceExpiryDate").isEqualTo(licenceExpiryDate)
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo(PPUD_VALID_MAPPA_LEVEL_2)
      .jsonPath("offender.sentences[0].releaseDate").isEqualTo(releaseDate)
      .jsonPath("offender.sentences[0].sentencedUnder").isEqualTo("Not Specified")
      .jsonPath("offender.sentences[0].sentenceExpiryDate").isEqualTo(sentenceExpiryDate)
      .jsonPath("offender.sentences[0].sentenceLength.partYears").isEqualTo(sentenceLengthPartYears)
      .jsonPath("offender.sentences[0].sentenceLength.partMonths").isEqualTo(sentenceLengthPartMonths)
      .jsonPath("offender.sentences[0].sentenceLength.partDays").isEqualTo(sentenceLengthPartDays)
      .jsonPath("offender.sentences[0].sentencingCourt").isEqualTo(sentencingCourt)
  }

  private fun findSentenceIdOnOffender(offenderId: String): String {
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(offenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!
    return sentenceId
  }

  private fun testPutSentence(offenderId: String, sentenceId: String, requestBody: String) {
    putSentence(offenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()
      .isEmpty
  }

  private fun putSentence(offenderId: String, sentenceId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUpdateSentenceUri(offenderId, sentenceId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUpdateSentenceUri(offenderId: String, sentenceId: String) =
    "/offender/$offenderId/sentence/$sentenceId"
}
