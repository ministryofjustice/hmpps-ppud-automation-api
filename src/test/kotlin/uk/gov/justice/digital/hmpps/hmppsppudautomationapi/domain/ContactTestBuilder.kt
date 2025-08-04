package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun contact(name: String? = randomString(), faxEmail: String? = randomString()) = Contact(name, faxEmail)
