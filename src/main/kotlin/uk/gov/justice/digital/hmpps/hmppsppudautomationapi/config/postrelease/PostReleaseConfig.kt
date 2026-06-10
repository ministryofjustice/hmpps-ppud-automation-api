package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class PostReleaseConfig(
  @param:Value("\${ppud.release.postRelease.licenceType.determinate}") val determinateLicenceType: String,
  @param:Value("\${ppud.release.postRelease.licenceType.dcr}") val dcrLicenceType: String,
  @param:Value("\${ppud.release.postRelease.licenceType.ipp}") val ippLicenceType: String,
  @param:Value("\${ppud.release.postRelease.licenceType.life}") val lifeLicenceType: String,
)
