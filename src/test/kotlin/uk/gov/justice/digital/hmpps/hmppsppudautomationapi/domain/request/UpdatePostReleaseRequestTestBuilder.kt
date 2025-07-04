package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ContactWithTelephone
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.contactWithTelephone
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun updatePostReleaseRequest(
  assistantChiefOfficer: Contact = contact(),
  offenderManager: ContactWithTelephone = contactWithTelephone(),
  probationService: String? = randomString(),
  spoc: Contact = contact(),
) = UpdatePostReleaseRequest(assistantChiefOfficer, offenderManager, probationService, spoc)
