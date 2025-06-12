package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class PostReleaseConfig(
  @Value("\${ppud.release.postRelease.licenceType.determinate}") val determinateLicenceType: String,
  @Value("\${ppud.release.postRelease.licenceType.ipp}") val ippLicenceType: String,
  @Value("\${ppud.release.postRelease.licenceType.life}") val lifeLicenceType: String,
)
