package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

  @Mock
  private lateinit var uuidProvider: () -> UUID

  private lateinit var documentService: DocumentService

  companion object {

    private const val STORAGE_DIRECTORY = "./build/tmp/docs"

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      val directory = File(STORAGE_DIRECTORY)
      if (directory.exists()) {
        deleteDirectory(directory)
      } else {
        directory.mkdir()
      }
    }

    private fun deleteDirectory(directory: File) {
      if (directory.exists()) {
        directory.listFiles()?.forEach {
          if (it.isDirectory) {
            deleteDirectory(it)
          } else {
            it.delete()
          }
        }
      }
      directory.delete()
    }
  }

  @BeforeEach
  fun beforeEach() {
    documentService = DocumentService(documentManagementClient, uuidProvider, STORAGE_DIRECTORY)
  }

  @Test
  fun `given document ID when downloadDocument called then document is retrieved from Document Management API`() {
    runBlocking {
      val documentId = UUID.randomUUID()
      given(documentManagementClient.retrieveDocument(documentId)).willReturn(RetrievedFile(ByteArray(0), "empty.txt"))
      given(uuidProvider.invoke()).willReturn(UUID.randomUUID())

      documentService.downloadDocument(documentId)

      then(documentManagementClient).should().retrieveDocument(documentId)
    }
  }

  @Test
  fun `given document ID when downloadDocument called then document is saved to unique directory and absolute path returned`() {
    runBlocking {
      val documentId = UUID.randomUUID()
      val filename = randomString("filename") + ".pdf"
      val uniqueSubdirectory = UUID.randomUUID()
      val filepath = "$STORAGE_DIRECTORY/$uniqueSubdirectory/$filename"
      val absolutePath = Paths.get(filepath).absolutePathString()
      val streamedFile = ClassPathResource("test-file.pdf").contentAsByteArray
      given(documentManagementClient.retrieveDocument(documentId)).willReturn(RetrievedFile(streamedFile, filename))
      given(uuidProvider.invoke()).willReturn(uniqueSubdirectory)

      val actualFilepath = documentService.downloadDocument(documentId)

      assertEquals(absolutePath, actualFilepath)
      assertTrue(File(actualFilepath).isFile, "Document does not exist at '$absolutePath'")
    }
  }

  @Test
  fun `given document filepath and existing file when deleteDocument called then document and parent folder are deleted`() {
    runBlocking {
      val filename = randomString("filename") + ".txt"
      val uniqueSubdirectory = UUID.randomUUID()
      val filepath = "$STORAGE_DIRECTORY/$uniqueSubdirectory/$filename"
      val absolutePath = Paths.get(filepath).absolutePathString()
      File(STORAGE_DIRECTORY).mkdir()
      File(filepath).parentFile.mkdir()
      File(filepath).createNewFile()

      documentService.deleteDownloadedDocument(filepath)

      //  assertFalse(File(absolutePath).isFile, "Document still exists at '$absolutePath'")
      assertFalse(File(absolutePath).parentFile.isDirectory, "Parent directory still exists for file '$absolutePath'")
    }
  }
}
