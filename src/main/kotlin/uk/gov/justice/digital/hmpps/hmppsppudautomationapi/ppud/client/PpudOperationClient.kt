package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import org.openqa.selenium.WebDriverException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.err.PpudErrorHandlerClient
import java.util.function.Supplier

@Service
class PpudOperationClient {

  @Autowired
  private lateinit var errorHandler: PpudErrorHandlerClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun <T> invoke(retryOnFailure: Boolean, operation: Supplier<T>): T {
    return if (!retryOnFailure) {
      operation.get()
    } else {
      invokeWithRetry(operation)
    }
  }

  private suspend fun <T> invokeWithRetry(operation: Supplier<T>): T {
    return try {
      operation.get()
    } catch (webDriverException: WebDriverException) {
      val exceptionToLog = errorHandler.handleException(webDriverException)
      log.error("Exception occurred but operation will be retried", exceptionToLog)
      operation.get()
    }
  }
}