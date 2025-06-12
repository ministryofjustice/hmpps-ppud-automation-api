package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.release.ReleaseConfig

/**
 * This enum refers to the "Released Under" values supported by CaR in terms of
 * creating and updating releases. There are more values in PPUD which are
 * ignored/excluded in one way or another (if not directly in this service, in
 * the API or UI services).
 */
enum class SupportedReleasedUnder {
  IPP_LICENCE {
    override fun getFullName(releaseConfig: ReleaseConfig): String = releaseConfig.ippLicence
  },
  LIFE_LICENCE {
    override fun getFullName(releaseConfig: ReleaseConfig): String = releaseConfig.lifeLicence
  }, ;

  abstract fun getFullName(releaseConfig: ReleaseConfig): String
}
