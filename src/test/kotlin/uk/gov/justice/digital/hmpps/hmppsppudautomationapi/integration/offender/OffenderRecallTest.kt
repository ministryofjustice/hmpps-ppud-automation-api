package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.IsSameDayAs
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.isNull
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.withoutSeconds
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidMappaLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidPoliceForce
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidProbationArea
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidUserFullName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidUserFullNameAndTeam
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.function.Consumer
import java.util.stream.Stream

class OffenderRecallTest : IntegrationTestBase() {

  companion object {

    private const val ppudExpectedRecallType = "Standard"

    private const val ppudExpectedOwningTeam = "Recall 1"

    private const val ppudExpectedRevocationIssuedByOwner = "EO Officer"

    private const val ppudExpectedReturnToCustodyNotificationMethod = "Not Applicable"

    // This is an offender that exists in PPUD InternalTest
    private val ppudOffenderWithRelease: TestOffender
      get() = TestOffender(
        id = "4F6666656E64657269643D313632393134G721H665",
        sentenceDate = "2003-06-12",
        releaseDate = "2013-02-02",
      )

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("decisionDateTime", createRecallRequestBody(decisionDateTime = "")),
        MandatoryFieldTestData("mappaLevel", createRecallRequestBody(mappaLevel = "")),
        MandatoryFieldTestData("policeForce", createRecallRequestBody(policeForce = "")),
        MandatoryFieldTestData("probationArea", createRecallRequestBody(probationArea = "")),
        MandatoryFieldTestData("receivedDateTime", createRecallRequestBody(receivedDateTime = "")),
        MandatoryFieldTestData("recommendedToOwner", createRecallRequestBody(recommendedToOwner = "")),
        MandatoryFieldTestData("releaseDate", createRecallRequestBody(releaseDate = "")),
        MandatoryFieldTestData(
          "riskOfSeriousHarmLevel",
          createRecallRequestBody(riskOfSeriousHarmLevel = ""),
          errorFragment = "RiskOfSeriousHarmLevel",
        ),
        MandatoryFieldTestData("sentenceDate", createRecallRequestBody(sentenceDate = "")),
      )
    }

    private fun createRecallRequestBody(
      decisionDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      isInCustody: String = "false",
      isExtendedSentence: String = "false",
      mappaLevel: String = ppudValidMappaLevel,
      policeForce: String = ppudValidPoliceForce,
      probationArea: String = ppudValidProbationArea,
      receivedDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      recommendedToOwner: String = ppudValidUserFullNameAndTeam,
      releaseDate: String = ppudOffenderWithRelease.releaseDate,
      riskOfContrabandDetails: String = "",
      riskOfSeriousHarmLevel: String = RiskOfSeriousHarmLevel.VeryHigh.name,
      sentenceDate: String = ppudOffenderWithRelease.sentenceDate,
    ): String {
      return "{" +
        "\"decisionDateTime\":\"${decisionDateTime}\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"isExtendedSentence\":\"$isExtendedSentence\", " +
        "\"mappaLevel\":\"${mappaLevel}\", " +
        "\"policeForce\":\"${policeForce}\", " +
        "\"probationArea\":\"$probationArea\", " +
        "\"receivedDateTime\":\"${receivedDateTime}\", " +
        "\"recommendedToOwner\":\"$recommendedToOwner\", " +
        "\"releaseDate\":\"$releaseDate\", " +
        "\"riskOfContrabandDetails\":\"$riskOfContrabandDetails\", " +
        "\"riskOfSeriousHarmLevel\":\"$riskOfSeriousHarmLevel\", " +
        "\"sentenceDate\":\"$sentenceDate\" " +
        "}"
    }
  }

  @Test
  fun `given missing request body when recall called then bad request is returned`() {
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when recall called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(data.requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given complete set of valid values in request body when recall called then 201 created and recall Id are returned`() {
    val requestBody = createRecallRequestBody()
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("recall.id").isNotEmpty()
  }

  @Test
  fun `given valid values in request body when recall called then recall is created using supplied values`() {
    val decisionDateTime = randomTimeToday()
    val receivedDateTime = randomTimeToday().truncatedTo(ChronoUnit.MINUTES)
    val requestBody = createRecallRequestBody(
      decisionDateTime = decisionDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      receivedDateTime = receivedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )

    val id = postRecall(requestBody)

    val retrieved = retrieveRecall(id)
    retrieved.jsonPath("recall.id").isEqualTo(id)
      .jsonPath("recall.allMandatoryDocumentsReceived").isEqualTo("No")
      .jsonPath("recall.decisionDateTime").isEqualTo(decisionDateTime.withoutSeconds())
      .jsonPath("recall.mappaLevel").isEqualTo(ppudValidMappaLevel)
      .jsonPath("recall.owningTeam").isEqualTo(ppudExpectedOwningTeam)
      .jsonPath("recall.policeForce").isEqualTo(ppudValidPoliceForce)
      .jsonPath("recall.probationArea").isEqualTo(ppudValidProbationArea)
      .jsonPath("recall.receivedDateTime").isEqualTo(receivedDateTime.withoutSeconds())
      .jsonPath("recall.recommendedToOwner").isEqualTo(ppudValidUserFullName)
      .jsonPath("recall.recallType").isEqualTo(ppudExpectedRecallType)
      .jsonPath("recall.revocationIssuedByOwner").isEqualTo(ppudExpectedRevocationIssuedByOwner)
    val recommendedToDateTimeIsToday = IsSameDayAs(LocalDate.now())
    retrieved.jsonPath("recall.recommendedToDateTime").value(recommendedToDateTimeIsToday)
    assertTrue(recommendedToDateTimeIsToday.isSameDay, "recommendedToDateTime is not today")
  }

  @Test
  fun `given offender is already in custody when recall called then UAL is unchecked UAL check is not set and return to custody is set`() {
    val requestBody = createRecallRequestBody(isInCustody = "true")

    val id = postRecall(requestBody)

    val retrieved = retrieveRecall(id)
    retrieved
      .jsonPath("recall.isInCustody").isEqualTo("true")
      .jsonPath("recall.nextUalCheck").value(isNull())
      .jsonPath("recall.returnToCustodyNotificationMethod")
      .isEqualTo(ppudExpectedReturnToCustodyNotificationMethod)
  }

  @Test
  fun `given offender is not in custody when recall called then UAL is checked UAL check is set and return to custody is not set`() {
    val requestBody = createRecallRequestBody(isInCustody = "false")
    val id = postRecall(requestBody)

    val retrieved = retrieveRecall(id)
    retrieved
      .jsonPath("recall.isInCustody").isEqualTo("false")
      .jsonPath("recall.nextUalCheck")
      .isEqualTo(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE))
      .jsonPath("recall.returnToCustodyNotificationMethod").isEqualTo("Not Specified")
  }

  @Test
  fun `given risk of contraband details when recall called then 201 created and recall Id are returned`() {
    val requestBody = createRecallRequestBody(riskOfContrabandDetails = randomString("riskOfContrabandDetails"))
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
  }

  private fun postRecall(requestBody: String): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("recall.id").value(idExtractor)
    val id = idExtractor.value
    assertNotNull(id, "ID returned from create recall request is null")
    assertTrue(id!!.isNotEmpty(), "ID returned from create recall request is empty")
    return id
  }

  private fun retrieveRecall(id: String): BodyContentSpec {
    return webTestClient.get()
      .uri("/recall/$id")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }

  class TestOffender(
    val id: String,
    val sentenceDate: String,
    val releaseDate: String,
  )
}
