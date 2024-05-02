package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.DocumentManagementClient
import java.io.File
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.pathString

@Component
internal class DocumentService(
  private val documentManagementClient: DocumentManagementClient,
  @Value("\${documents.storageDirectory}") private val documentsStorageDirectory: String,
) {

  fun downloadDocument(documentId: UUID): String {
    val retrievedFile = documentManagementClient.retrieveDocument(documentId)
    val filepath = Paths.get(documentsStorageDirectory, retrievedFile.filename)
    val file = File(filepath.pathString)
    file.writeBytes(retrievedFile.content)
    return file.absolutePath
  }
}
