package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("ppud")
class PpudHealth(
  @Qualifier(value = "ppudHealthCheckWebClient") private val webClient: WebClient,
  @Value("\${ppud.health.path}") private val path: String,
) : PingHealthCheck(webClient, path)
