package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

  @Bean
  fun webClientNoAuthNoMetrics(): WebClient {
    return WebClient.create()
  }
}
