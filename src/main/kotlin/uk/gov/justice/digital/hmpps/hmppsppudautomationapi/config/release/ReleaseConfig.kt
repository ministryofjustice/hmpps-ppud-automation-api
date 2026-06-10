package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.release

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class ReleaseConfig(
  @param:Value("\${ppud.release.releasedUnder.ippLicence}") val ippLicence: String,
  @param:Value("\${ppud.release.releasedUnder.lifeLicence}") val lifeLicence: String,
)
