package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.recall

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockserver.model.HttpRequest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream

class RecallAdditionalDocumentUploadTest : IntegrationTestBase() {

  private lateinit var offenderId: String

  private lateinit var releaseId: String

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("documentId", uploadAdditionalDocumentRequestBody(documentId = "")),
        MandatoryFieldTestData("title", uploadAdditionalDocumentRequestBody(title = ""), "title"),
      )
    }

    fun uploadAdditionalDocumentRequestBody(
      documentId: String = UUID.randomUUID().toString(),
      title: String? = randomString("title"),
    ): String {
      return """
        { 
        "documentId":"$documentId", 
        "title":"$title"
        }
      """.trimIndent()
    }
  }

  @BeforeAll
  fun beforeAll() {
    offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    releaseId = createTestReleaseInPpud(offenderId, sentenceId)
    startupMockServers()
  }

  @BeforeEach
  fun beforeEach() {
    resetMockServers()
  }

  @AfterAll
  fun afterAll() {
    tearDownMockServers()
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
  }

  @Test
  fun `given missing request body when upload additional document called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUri(randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when upload additional document called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    putAdditionalDocument(randomPpudId(), data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing token when upload additional document called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, constructUri(randomPpudId()))
  }

  @Test
  fun `given token without recall role when upload additional document called then forbidden is returned`() {
    val requestBody = uploadAdditionalDocumentRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      constructUri(randomPpudId()),
      requestBody,
      HttpMethod.PUT,
    )
  }

  @Test
  fun `given valid values in request body when upload additional document called then document is uploaded`() {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val documentId = UUID.randomUUID()
    setupDocumentManagementMockToReturnDocument(documentId)
    val title = randomString("title")
    val requestBody = uploadAdditionalDocumentRequestBody(
      documentId = documentId.toString(),
      title = title,
    )

    putAdditionalDocument(recallId, requestBody)
      .expectStatus().isOk

    documentManagementMock.verify(HttpRequest.request().withPath("/documents/$documentId/file"))
    val retrievedRecall = retrieveRecall(recallId)
    val expectedDocumentType = "216 - Post Release Recall" // As configured for InternalTest
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.documents.size()").isEqualTo(1)
      .jsonPath("recall.documents[0].title").isEqualTo(title)
      .jsonPath("recall.documents[0].documentType").isEqualTo(expectedDocumentType)
  }

  private fun putAdditionalDocument(recallId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUri(recallId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUri(recallId: String) = "/recall/$recallId/additional-document"
}
