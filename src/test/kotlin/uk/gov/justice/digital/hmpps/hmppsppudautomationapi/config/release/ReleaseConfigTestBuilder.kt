package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.release

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

fun releaseConfig(ippLicence: String = randomString(), lifeLicence: String = randomString()) = ReleaseConfig(ippLicence, lifeLicence)
