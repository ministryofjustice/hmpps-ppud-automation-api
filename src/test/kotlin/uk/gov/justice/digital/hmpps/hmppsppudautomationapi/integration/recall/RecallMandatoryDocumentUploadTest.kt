package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.recall

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.doesNotContain
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream

class RecallMandatoryDocumentUploadTest : IntegrationTestBase() {

  private lateinit var offenderId: String

  private lateinit var releaseId: String

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("documentId", uploadMandatoryDocumentRequestBody(documentId = "")),
        MandatoryFieldTestData("category", uploadMandatoryDocumentRequestBody(category = ""), "DocumentCategory"),
      )
    }

    fun uploadMandatoryDocumentRequestBody(
      documentId: String = UUID.randomUUID().toString(),
      category: String? = randomDocumentCategory().toString(),
    ): String {
      return """
        { 
        "documentId":"$documentId", 
        "category":"$category"
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
  fun `given missing request body when upload document called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUri(randomPpudId()))
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
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, constructUri(randomPpudId()))
  }

  @Test
  fun `given token without recall role when upload document called then forbidden is returned`() {
    val requestBody = uploadMandatoryDocumentRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      constructUri(randomPpudId()),
      requestBody,
      HttpMethod.PUT,
    )
  }

  @Test
  fun `given valid values in request body when upload document called then document is uploaded and document is marked as not missing`() {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val documentId = UUID.randomUUID()
    setupDocumentManagementMockToReturnDocument(documentId)
    val documentCategory = randomDocumentCategory()
    val requestBody = uploadMandatoryDocumentRequestBody(
      documentId = documentId.toString(),
      category = documentCategory.toString(),
    )

    putDocument(recallId, requestBody)
      .expectStatus().isOk

    documentManagementMock.verify(HttpRequest.request().withPath("/documents/$documentId/file"))
    val retrievedRecall = retrieveRecall(recallId)
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.documents.size()").isEqualTo(1)
      .jsonPath("recall.documents[0].title").isEqualTo(documentCategory.title)
      .jsonPath("recall.documents[0].documentType").isEqualTo("216 - Post Release Recall")
      .jsonPath("recall.missingMandatoryDocuments.size()").isEqualTo(5)
      .jsonPath("recall.missingMandatoryDocuments").value(doesNotContain(documentCategory.toString()))
  }

  @Test
  fun `given last mandatory document when upload document called then document is uploaded and all documents are marked as not missing`() {
    val recallId = createTestRecallInPpud(offenderId, releaseId)
    val documentId = UUID.randomUUID()
    setupDocumentManagementMockToReturnDocument(documentId)

    for (category in DocumentCategory.entries.shuffled()) {
      val requestBody = uploadMandatoryDocumentRequestBody(
        documentId = documentId.toString(),
        category = category.toString(),
      )
      putDocument(recallId, requestBody)
        .expectStatus().isOk
    }

    val retrievedRecall = retrieveRecall(recallId)
    retrievedRecall
      .jsonPath("recall.id").isEqualTo(recallId)
      .jsonPath("recall.documents.size()").isEqualTo(6)
      .jsonPath("recall.missingMandatoryDocuments.size()").isEqualTo(0)
      .jsonPath("recall.allMandatoryDocumentsReceived").isEqualTo("Yes")
  }

  private fun setupDocumentManagementMockToReturnDocument(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/file")
    documentManagementMock.`when`(request).respond(
      HttpResponse.response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-file.pdf\"")
        .withBody(ClassPathResource("test-file.pdf").file.readBytes()),
    )
  }

  private fun putDocument(recallId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUri(recallId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUri(recallId: String) = "/recall/$recallId/mandatory-document"
}
