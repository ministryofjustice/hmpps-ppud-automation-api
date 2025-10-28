package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.flipt.client.FliptClient
import io.flipt.client.models.ClientTokenAuthentication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.temporal.ChronoUnit

@Configuration
@ConfigurationProperties(prefix = "flipt")
class FliptConfig {

  lateinit var url: String
  lateinit var token: String
  var defaultFlagValue: Boolean = false // can't use lateinit with primitives, so defaulting

  @Bean
  fun fliptApiClient(): FliptClient =
    FliptClient
      .builder()
      .namespace("consider-a-recall").url(url)
      .authentication(ClientTokenAuthentication(token))
      // we deal with time-sensitive flags, so need a short cache duration
      .updateInterval(Duration.of(1, ChronoUnit.SECONDS))
      .build()
}