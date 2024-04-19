package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("documentManagement")
class DocumentManagementHealth(
  @Qualifier("documentManagementHealthCheckWebClient") webClient: WebClient,
) : PingHealthCheck(webClient, "/health/ping")
