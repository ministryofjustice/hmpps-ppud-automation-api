package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.recall

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer
import java.util.stream.Stream

class RecallMinuteTest : IntegrationTestBase() {

  private lateinit var offenderId: String

  private lateinit var releaseId: String

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("subject", addMinuteRequestBody(subject = "")),
        MandatoryFieldTestData("text", addMinuteRequestBody(text = "")),
      )
    }

    fun addMinuteRequestBody(
      subject: String = randomString("subject"),
      text: String = randomString("text"),
    ): String {
      return """
        { 
          "subject":"$subject", 
          "text":"$text"
        }
      """.trimIndent()
    }
  }

  @BeforeAll
  fun beforeAll() {
    offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    releaseId = createTestReleaseInPpud(offenderId, sentenceId)
  }

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
  }

  @Test
  fun `given missing request body when add minute called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUri(randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when add minute called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    putMinute(randomPpudId(), data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing token when add minute called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, constructUri(randomPpudId()))
  }

  @Test
  fun `given token without recall role when add minute called then forbidden is returned`() {
    val requestBody = addMinuteRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      constructUri(randomPpudId()),
      requestBody,
      HttpMethod.PUT,
    )
  }

  @Test
  fun `given valid values in request body when add minute called then minute is created`() {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val subject = randomString("subject")
    val text = randomString("text line 1")
    val requestBody = addMinuteRequestBody(subject, text)

    putMinute(recallId, requestBody)
      .expectStatus().isOk

    val retrievedRecall = retrieveRecall(recallId)
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.minutes.size()").isEqualTo(2)
      .jsonPath("recall.minutes[1].subject").isEqualTo(subject)
      .jsonPath("recall.minutes[1].text").isEqualTo(text)
  }

  @Test
  fun `given repeated request with same values in request body when add minute called then minute is not created`() {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val subject = randomString("subject")
    val text = randomString("text line 1")
    val requestBody = addMinuteRequestBody(subject, text)

    putMinute(recallId, requestBody).expectStatus().isOk
    putMinute(recallId, requestBody).expectStatus().isOk

    val retrievedRecall = retrieveRecall(recallId)
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.minutes.size()").isEqualTo(2)
  }

  @ParameterizedTest
  @ValueSource(strings = ["\\n", "\\r\\n"])
  fun `given minute text with line breaks in request body when add minute called then line breaks are preserved`(
    separator: String,
  ) {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val subject = randomString("subject")
    val text = "123${separator}456"
    val requestBody = addMinuteRequestBody(subject, text)

    putMinute(recallId, requestBody)
      .expectStatus().isOk

    val retrievedRecall = retrieveRecall(recallId)
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.minutes.size()").isEqualTo(2)
      .jsonPath("recall.minutes[1].text").isEqualTo("123${System.lineSeparator()}456")
  }

  private fun putMinute(recallId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUri(recallId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUri(recallId: String) = "/recall/$recallId/minutes"
}
