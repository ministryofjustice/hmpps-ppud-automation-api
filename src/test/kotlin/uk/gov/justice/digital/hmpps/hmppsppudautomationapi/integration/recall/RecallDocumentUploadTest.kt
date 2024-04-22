package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.recall

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream

class RecallDocumentUploadTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("documentId", uploadDocumentRequestBody(documentId = "")),
        MandatoryFieldTestData("category", uploadDocumentRequestBody(category = "")),
      )
    }

    fun uploadDocumentRequestBody(
      documentId: String = UUID.randomUUID().toString(),
      category: String = randomDocumentCategory().toString(),
    ): String {
      return """
        { 
        "documentId":"$documentId", 
        "category":"$category"
        }
        """.trimIndent()
    }
  }

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
  }

  @Test
  fun `given missing request body when upload document called then bad request is returned`() {
    webTestClient.put()
      .uri("/recall/${randomPpudId()}/document")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when upload document called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    putDocument(randomPpudId(), data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing token when upload document called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, "/recall/${randomPpudId()}/document")
  }

  @Test
  fun `given token without recall role when upload document called then forbidden is returned`() {
    val requestBody = uploadDocumentRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      "/recall/${randomPpudId()}/document",
      requestBody,
      HttpMethod.PUT,
    )
  }

  protected fun putDocument(recallId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/recall/$recallId/document")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
}
