package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream

class OffenderRecallTest : IntegrationTestBase() {

  companion object {

    // This is an offender that exists in PPUD InternalTest
    @JvmStatic
    private val ppudOffenderWithRelease: TestOffender
      get() = TestOffender(
        id = "4F6666656E64657269643D313632393134G721H665",
        sentenceDate = "2003-06-12",
        releaseDate = "2013-02-02",
      )

    private const val ppudValidUserFullName = "Consider a Recall Test(Recall 1)"

    private const val ppudValidProbationArea = "Merseyside"

    private const val ppudValidPoliceForce = "Kent Police"

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("decisionDateTime", createRecallRequestBody(decisionDateTime = "")),
        MandatoryFieldTestData("policeForce", createRecallRequestBody(policeForce = "")),
        MandatoryFieldTestData("probationArea", createRecallRequestBody(probationArea = "")),
        MandatoryFieldTestData("receivedDateTime", createRecallRequestBody(receivedDateTime = "")),
        MandatoryFieldTestData("recommendedToOwner", createRecallRequestBody(recommendedToOwner = "")),
        MandatoryFieldTestData("releaseDate", createRecallRequestBody(releaseDate = "")),
        MandatoryFieldTestData("riskOfSeriousHarmLevel", createRecallRequestBody(riskOfSeriousHarmLevel = "")),
        MandatoryFieldTestData("sentenceDate", createRecallRequestBody(sentenceDate = "")),
      )
    }

    @JvmStatic
    private fun createRecallRequestBody(
      decisionDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_DATE_TIME),
      isInCustody: String = "false",
      isExtendedSentence: String = "false",
      policeForce: String = ppudValidPoliceForce,
      probationArea: String = ppudValidProbationArea,
      receivedDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_DATE_TIME),
      recommendedToOwner: String = ppudValidUserFullName,
      releaseDate: String = ppudOffenderWithRelease.releaseDate,
      riskOfSeriousHarmLevel: String = randomString("rosh"),
      sentenceDate: String = ppudOffenderWithRelease.sentenceDate,
    ): String {
      return "{" +
        "\"decisionDateTime\":\"${decisionDateTime}\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"isExtendedSentence\":\"$isExtendedSentence\", " +
        "\"policeForce\":\"${policeForce}\", " +
        "\"probationArea\":\"$probationArea\", " +
        "\"receivedDateTime\":\"${receivedDateTime}\", " +
        "\"recommendedToOwner\":\"$recommendedToOwner\", " +
        "\"releaseDate\":\"$releaseDate\", " +
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
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(data.requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(data.propertyName) })
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
  fun `given offender is already in custody when recall called then 201 created and recall Id are returned`() {
    val requestBody = createRecallRequestBody(isInCustody = "true")
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
  }

  class TestOffender(
    val id: String,
    val sentenceDate: String,
    val releaseDate: String,
  )

  class MandatoryFieldTestData(
    val propertyName: String,
    val requestBody: String,
  )
}
