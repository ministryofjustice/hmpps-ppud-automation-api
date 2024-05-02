package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.DocumentManagementClient
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

@Component
internal class DocumentService(
  private val documentManagementClient: DocumentManagementClient,
  private val uuidProvider: () -> UUID,
  @Value("\${documents.storageDirectory}") private val documentsStorageDirectory: String,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun downloadDocument(documentId: UUID): String {
    val retrievedFile = documentManagementClient.retrieveDocument(documentId)
    val directoryPath = Paths.get(documentsStorageDirectory, uuidProvider.invoke().toString())
    ensureDirectoryExists(directoryPath)
    val absoluteFilepath = Paths.get(directoryPath.absolutePathString(), retrievedFile.filename).absolutePathString()
    val file = File(absoluteFilepath)
    file.writeBytes(retrievedFile.content)
    return absoluteFilepath
  }

  fun deleteDownloadedDocument(pathToDownloadedDocument: String) {
    try {
      val file = File(pathToDownloadedDocument)
      file.delete()
      file.parentFile.delete()
    } catch (ex: Exception) {
      log.error("Downloaded file or containing directory could not be deleted.", ex)
    }
  }

  private fun ensureDirectoryExists(directoryPath: Path) {
    if (!directoryPath.exists()) {
      ensureDirectoryExists(directoryPath.parent)
      File(directoryPath.absolutePathString()).mkdir()
    }
  }
}
