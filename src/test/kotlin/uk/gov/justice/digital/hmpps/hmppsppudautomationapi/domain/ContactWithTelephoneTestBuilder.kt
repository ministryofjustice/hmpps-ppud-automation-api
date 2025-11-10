package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun contactWithTelephone(
  name: String? = randomString(),
  faxEmail: String? = randomString(),
  telephone: String? = randomString(),
) = ContactWithTelephone(name, faxEmail, telephone)
