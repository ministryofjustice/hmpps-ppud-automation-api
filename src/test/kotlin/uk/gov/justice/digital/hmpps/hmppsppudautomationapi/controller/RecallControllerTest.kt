package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.DocumentService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateAddMinuteRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class RecallControllerTest {

  @Mock
  lateinit var ppudClient: OperationalPpudClient

  @Mock
  lateinit var documentService: DocumentService

  private lateinit var controller: RecallController

  @BeforeEach
  fun beforeEach() {
    controller = RecallController(documentService, ppudClient)
  }

  @Test
  fun `given recall ID when get is called then PPUD client is called with ID`() {
    runBlocking {
      val id = randomPpudId()
      given(ppudClient.retrieveRecall(id)).willReturn(generateRecall(id = id))

      controller.get(id)

      then(ppudClient).should().retrieveRecall(id)
    }
  }

  @Test
  fun `given recall id and document data when uploadMandatoryDocument is called then document is downloaded and data is passed to PPUD client`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadMandatoryDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)

      controller.uploadMandatoryDocument(recallId, request)

      then(documentService).should()
        .downloadDocument(documentId)
      then(ppudClient).should()
        .uploadMandatoryDocument(recallId, request, pathToDownloadedDocument)
    }
  }

  @Test
  fun `given document uploaded successfully when uploadMandatoryDocument is called then downloaded document is deleted`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadMandatoryDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)

      controller.uploadMandatoryDocument(recallId, request)

      val inOrder = inOrder(ppudClient, documentService)
      then(ppudClient).should(inOrder)
        .uploadMandatoryDocument(any(), any(), any())
      then(documentService).should(inOrder)
        .deleteDownloadedDocument(pathToDownloadedDocument)
    }
  }

  @Test
  fun `given document upload fails when uploadMandatoryDocument is called then downloaded document is deleted`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadMandatoryDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)
      given(ppudClient.uploadMandatoryDocument(any(), any(), any())).willThrow(RuntimeException("Test Exception"))

      assertThrows<RuntimeException> {
        controller.uploadMandatoryDocument(recallId, request)
      }

      then(documentService).should()
        .deleteDownloadedDocument(pathToDownloadedDocument)
    }
  }

  @Test
  fun `given recall id and document data when uploadAdditionalDocument is called then document is downloaded and data is passed to PPUD client`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadAdditionalDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)

      controller.uploadAdditionalDocument(recallId, request)

      then(documentService).should()
        .downloadDocument(documentId)
      then(ppudClient).should()
        .uploadAdditionalDocument(recallId, request, pathToDownloadedDocument)
    }
  }

  @Test
  fun `given document uploaded successfully when uploadAdditionalDocument is called then downloaded document is deleted`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadAdditionalDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)

      controller.uploadAdditionalDocument(recallId, request)

      val inOrder = inOrder(ppudClient, documentService)
      then(ppudClient).should(inOrder)
        .uploadAdditionalDocument(any(), any(), any())
      then(documentService).should(inOrder)
        .deleteDownloadedDocument(pathToDownloadedDocument)
    }
  }

  @Test
  fun `given document upload fails when uploadAdditionalDocument is called then downloaded document is deleted`() {
    runBlocking {
      val recallId = randomPpudId()
      val documentId = UUID.randomUUID()
      val request = generateUploadAdditionalDocumentRequest(documentId = documentId)
      val pathToDownloadedDocument = "some/path/doc.doc"
      given(documentService.downloadDocument(documentId)).willReturn(pathToDownloadedDocument)
      given(ppudClient.uploadAdditionalDocument(any(), any(), any())).willThrow(RuntimeException("Test Exception"))

      assertThrows<RuntimeException> {
        controller.uploadAdditionalDocument(recallId, request)
      }

      then(documentService).should()
        .deleteDownloadedDocument(pathToDownloadedDocument)
    }
  }

  @Test
  fun `given recall id and minute data when addMinute is called then data is passed to PPUD client`() {
    runBlocking {
      val recallId = randomPpudId()
      val request = generateAddMinuteRequest()

      controller.addMinute(recallId, request)

      then(ppudClient).should().addMinute(recallId, request)
    }
  }
}
