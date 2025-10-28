package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag

import io.flipt.client.FliptClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.FliptConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FeatureFlagService(
  private val client: FliptClient?,
  private val config: FliptConfig,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    // this is the pattern recognised by Flipt
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  }


  fun enabled(key: String) = try {
    if (client == null) {
      log.warn("Flipt client not configured, returning default value: ${config.defaultFlagValue}")
      config.defaultFlagValue
    } else {
      val currentDateTime = LocalDateTime.now().format(dateTimeFormatter)
      client
        .evaluateBoolean(key, key, mapOf("currentDateTime" to currentDateTime))
        .isEnabled
    }
  } catch (e: Exception) {
    throw FeatureFlagException(key, e)
  }

  class FeatureFlagException(key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}