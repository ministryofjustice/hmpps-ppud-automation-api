package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

// TODO re-organise properties, putting below ones under 'client' (e.g. ppud.client.url)
@Configuration
internal data class PpudClientConfig (
  @Value("\${ppud.url}") val url: String,
)