package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.auth

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun ppudAuthConfig(
  ppudUsername: String = randomString(),
  ppudPassword: String = randomString(),
  ppudAdminUsername: String = randomString(),
  ppudAdminPassword: String = randomString(),
): PpudAuthConfig = PpudAuthConfig(
  ppudUsername,
  ppudPassword,
  ppudAdminUsername,
  ppudAdminPassword,
)
