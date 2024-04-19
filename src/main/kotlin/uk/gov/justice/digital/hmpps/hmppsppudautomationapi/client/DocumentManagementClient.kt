package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.WebClientConfiguration.Companion.withRetry
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.DocumentNotFoundException
import java.io.ByteArrayInputStream
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeoutException

class DocumentManagementClient(
  private val webClient: WebClient,
  private val documentManagementTimeout: Long,
) {
  fun retrieveDocument(documentId: UUID): ByteArrayInputStream {
    val response = webClient
      .get()
      .uri("/documents/$documentId/file")
      .retrieve()
      .bodyToMono(ByteArray::class.java)
      .timeout(Duration.ofSeconds(documentManagementTimeout))
      .doOnError { ex ->
        handleException(ex, documentId)
      }
      .withRetry()
      .block()

    return ByteArrayInputStream(response)
  }

  private fun handleException(
    exception: Throwable?,
    documentId: UUID,
  ) {
    when (exception) {
      is TimeoutException -> {
        throw ClientTimeoutException(
          "Document Management Client",
          "No response within $documentManagementTimeout seconds",
        )
      }
      is WebClientResponseException.NotFound -> {
        throw DocumentNotFoundException(documentId)
      }
    }
  }
}
