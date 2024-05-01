package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.RetrievedFile
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.io.File
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.absolutePathString

@ExtendWith(MockitoExtension::class)
class DocumentServiceTest {

  @Mock
  private lateinit var documentManagementClient: DocumentManagementClient

  private lateinit var documentService: DocumentService

  companion object {

    private const val STORAGE_DIRECTORY = "./build/tmp/docs"

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      val directory = File(STORAGE_DIRECTORY)
      if (directory.exists()) {
        directory.listFiles()?.forEach { it.delete() }
      } else {
        directory.mkdir()
      }
    }
  }

  @BeforeEach
  fun beforeEach() {
    documentService = DocumentService(documentManagementClient, STORAGE_DIRECTORY)
  }

  @Test
  fun `given document ID when downloadDocument called then document is retrieved from Document Management API`() {
    runBlocking {
      val documentId = UUID.randomUUID()
      given(documentManagementClient.retrieveDocument(documentId)).willReturn(RetrievedFile(ByteArray(0), "empty.txt"))

      documentService.downloadDocument(documentId)

      then(documentManagementClient).should().retrieveDocument(documentId)
    }
  }

  @Test
  fun `given document ID when downloadDocument called then document is saved to disk and absolute path returned`() {
    runBlocking {
      val documentId = UUID.randomUUID()
      val filename = randomString("filename") + ".pdf"
      val filepath = "$STORAGE_DIRECTORY/$filename"
      val absolutePath = Paths.get(filepath).absolutePathString()
      val streamedFile = ClassPathResource("test-file.pdf").contentAsByteArray
      given(documentManagementClient.retrieveDocument(documentId)).willReturn(RetrievedFile(streamedFile, filename))

      val actualFilepath = documentService.downloadDocument(documentId)

      assertEquals(absolutePath, actualFilepath)
      assertTrue(File(actualFilepath).isFile, "Document does not exist at '$absolutePath'")
    }
  }
}
