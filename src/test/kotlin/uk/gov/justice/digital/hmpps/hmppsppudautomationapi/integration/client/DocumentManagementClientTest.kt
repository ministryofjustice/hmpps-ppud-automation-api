package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.client

import org.hibernate.validator.internal.util.Contracts.assertNotEmpty
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.DocumentNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import java.util.UUID
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
class DocumentManagementClientTest(
  @Value("\${document-management.client.timeout}") private val timeout: Long,
) : IntegrationTestBase() {

  @Autowired
  private lateinit var documentManagementClient: DocumentManagementClient

  @Test
  fun `given document management service returns 500 error when retrieveDocument is called then an exception is thrown`() {
    val documentId = UUID.randomUUID()
    setupMockInternalServerErrorResponse(documentId)
    val exception = assertThrows<InternalServerError> {
      documentManagementClient.retrieveDocument(documentId)
    }
    assertTrue(
      exception.message?.startsWith("500 Internal Server Error") == true,
      "Exception message was '${exception.message}'",
    )
  }

  @Test
  fun `given document management service is timing out when retrieveDocument is called then an exception is thrown`() {
    val documentId = UUID.randomUUID()
    setupTimeoutResponse(documentId)
    assertThrows<ClientTimeoutException> {
      documentManagementClient.retrieveDocument(documentId)
    }
  }

  @Test
  fun `given non existing document ID when retrieveDocument is called then a DocumentNotFoundException is thrown`() {
    val nonExistentDocumentId = UUID.randomUUID()
    setupMockNotFoundResponse(nonExistentDocumentId)
    val exception = assertThrows<DocumentNotFoundException> {
      documentManagementClient.retrieveDocument(nonExistentDocumentId)
    }
    assertTrue(
      exception.message?.contains(nonExistentDocumentId.toString()) == true,
      "Exception message was '${exception.message}'",
    )
  }

  @Test
  fun `given existing document ID when retrieveDocument is called then document is returned`() {
    val documentId = UUID.randomUUID()
    setupMockDocumentResponse(documentId)
    val result = documentManagementClient.retrieveDocument(documentId)
    assertNotEmpty(result.readAllBytes(), "Document stream is empty")
  }

  @Test
  fun `given existing document ID and document management is intermittently failing when retrieveDocument is called then request is retried and document is returned`() {
    val documentId = UUID.randomUUID()
    setupMockDocumentResponseRequiringRetry(documentId)
    val result = documentManagementClient.retrieveDocument(documentId)
    assertNotEmpty(result.readAllBytes(), "Document stream is empty")
  }

  private fun setupMockDocumentResponse(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/file")
    documentManagementMock.`when`(request, Times.exactly(1)).respond(
      HttpResponse.response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-file.pdf\"")
        .withBody(ClassPathResource("test-file.pdf").file.readBytes()),
    )
  }

  private fun setupMockDocumentResponseRequiringRetry(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/file")
    documentManagementMock.`when`(request, Times.exactly(1))
      .respond(HttpResponse.response().withStatusCode(HttpStatusCode.GATEWAY_TIMEOUT_504.code()))
    documentManagementMock.`when`(request, Times.exactly(1)).respond(
      HttpResponse.response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-file.pdf\"")
        .withBody(ClassPathResource("test-file.pdf").file.readBytes()),
    )
  }

  private fun setupMockNotFoundResponse(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/personal-details")
    documentManagementMock.`when`(request, Times.exactly(1)).respond(
      HttpResponse.response().withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
    )
  }

  private fun setupMockInternalServerErrorResponse(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/file")
    documentManagementMock.`when`(request, Times.exactly(1)).respond(
      HttpResponse.response().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500.code()),
    )
  }

  private fun setupTimeoutResponse(documentId: UUID) {
    val request =
      HttpRequest.request().withPath("/documents/$documentId/file")
    documentManagementMock.`when`(request).respond(
      HttpResponse.response().withDelay(TimeUnit.SECONDS, timeout + 2)
        .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500.code()),
    )
  }
}
