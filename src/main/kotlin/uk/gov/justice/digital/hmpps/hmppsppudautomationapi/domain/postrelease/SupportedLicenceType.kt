package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.postrelease

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease.PostReleaseConfig

enum class SupportedLicenceType {
  DETERMINATE {
    override fun getFullName(postReleaseConfig: PostReleaseConfig): String = postReleaseConfig.determinateLicenceType
  },
  IPP {
    override fun getFullName(postReleaseConfig: PostReleaseConfig): String = postReleaseConfig.ippLicenceType
  },
  LIFE {
    override fun getFullName(postReleaseConfig: PostReleaseConfig): String = postReleaseConfig.lifeLicenceType
  },
  ;

  abstract fun getFullName(postReleaseConfig: PostReleaseConfig): String
}
