package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

fun postReleaseConfig(
  determinateLicenceType: String = randomString(),
  ippLicenceType: String = randomString(),
  lifeLicenceType: String = randomString(),
) = PostReleaseConfig(determinateLicenceType, ippLicenceType, lifeLicenceType)
