package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

// TODO re-organise properties, putting below ones under 'auth' (e.g. ppud.auth.username)
@Configuration
internal data class PpudAuthConfig(
  @Value("\${ppud.username}") val username: String,
  @Value("\${ppud.password}") val password: String,
  @Value("\${ppud.admin.username}") val adminUsername: String,
  @Value("\${ppud.admin.password}") val adminPassword: String,
)