package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.WebClientConfiguration.Companion.withRetry
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.DocumentNotFoundException
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeoutException

class DocumentManagementClient(
  private val webClient: WebClient,
  private val documentManagementTimeout: Long,
) {
  fun retrieveDocument(documentId: UUID): RetrievedFile {
    val response = webClient
      .get()
      .uri("/documents/$documentId/file")
      .retrieve()
      .onStatus(
        { statusCode -> statusCode == HttpStatus.NOT_FOUND },
        { throw DocumentNotFoundException(documentId) },
      )
      .toEntity(ByteArray::class.java)
      .timeout(Duration.ofSeconds(documentManagementTimeout))
      .doOnError(::handleException)
      .withRetry()
      .block()

    val contentDisposition = response!!.headers[CONTENT_DISPOSITION]?.first()
      ?: throw RuntimeException("Content disposition header not available from call to Document Management API.")
    val filename = ContentDisposition.parse(contentDisposition).filename
      ?: throw RuntimeException("Filename not available from call to Document Management API.")
    val content = response.body
      ?: throw RuntimeException("File content not available from call to Document Management API.")
    return RetrievedFile(content, filename)
  }

  private fun handleException(exception: Throwable?) {
    when (exception) {
      is TimeoutException -> {
        throw ClientTimeoutException(
          "Document Management Client",
          "No response within $documentManagementTimeout seconds",
        )
      }

      null -> throw RuntimeException("Null exception encountered calling Document Management API")

      else -> throw exception
    }
  }
}

data class RetrievedFile(
  val content: ByteArray,
  val filename: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RetrievedFile

    if (!content.contentEquals(other.content)) return false
    if (filename != other.filename) return false

    return true
  }

  override fun hashCode(): Int {
    var result = content.contentHashCode()
    result = 31 * result + filename.hashCode()
    return result
  }
}
