package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.flipt.client.FliptClient
import io.flipt.client.models.ClientTokenAuthentication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit

const val DEFAULT_POLLING_INTERVAL_IN_SECONDS = 60L

@Configuration
@ConfigurationProperties(prefix = "flipt")
class FliptConfig {

  lateinit var url: String
  lateinit var token: String
  var pollingIntervalInSeconds: Long =
    DEFAULT_POLLING_INTERVAL_IN_SECONDS // can't use lateinit with primitives, so defaulting
  var defaultFlagValue: Boolean = false // can't use lateinit with primitives, so defaulting

  @Bean
  fun fliptApiClient(): FliptClient =
    FliptClient
      .builder()
      .namespace("consider-a-recall").url(url)
      .authentication(ClientTokenAuthentication(token))
      .updateInterval(Duration.of(pollingIntervalInSeconds, ChronoUnit.SECONDS))
      .build()
}