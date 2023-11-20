package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream

class OffenderRecallTest : IntegrationTestBase() {

  companion object {

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
        MandatoryFieldTestData("sentenceDate", createRecallRequestBody(sentenceDate = "")),
        MandatoryFieldTestData("releaseDate", createRecallRequestBody(releaseDate = "")),
        MandatoryFieldTestData("recommendedToOwner", createRecallRequestBody(recommendedToOwner = "")),
        MandatoryFieldTestData("probationArea", createRecallRequestBody(probationArea = "")),
        MandatoryFieldTestData("decisionDateTime", createRecallRequestBody(decisionDateTime = "")),
        MandatoryFieldTestData("receivedDateTime", createRecallRequestBody(receivedDateTime = "")),
        MandatoryFieldTestData("policeForce", createRecallRequestBody(policeForce = "")),
      )
    }

    @JvmStatic
    private fun createRecallRequestBody(
      sentenceDate: String = ppudOffenderWithRelease.sentenceDate,
      releaseDate: String = ppudOffenderWithRelease.releaseDate,
      recommendedToOwner: String = ppudValidUserFullName,
      probationArea: String = ppudValidProbationArea,
      isInCustody: Boolean = false,
      decisionDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_DATE_TIME),
      receivedDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_DATE_TIME),
      policeForce: String = ppudValidPoliceForce,
    ): String {
      return "{" +
        "\"sentenceDate\":\"$sentenceDate\", " +
        "\"releaseDate\":\"$releaseDate\", " +
        "\"recommendedToOwner\":\"$recommendedToOwner\", " +
        "\"probationArea\":\"$probationArea\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"decisionDateTime\":\"${decisionDateTime}\", " +
        "\"receivedDateTime\":\"${receivedDateTime}\", " +
        "\"policeForce\":\"${policeForce}\" " +
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
  fun `given complete set of valid values in request body when recall called then created is returned`() {
    val requestBody = createRecallRequestBody()
    webTestClient.post()
      .uri("/offender/${ppudOffenderWithRelease.id}/recall")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
  }

  @Test
  fun `given offender is already in custody when recall called then created is returned`() {
    val requestBody = createRecallRequestBody(isInCustody = true)
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
