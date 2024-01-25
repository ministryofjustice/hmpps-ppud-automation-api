package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ContactWithTelephone

data class UpdatePostReleaseRequest(
  val assistantChiefOfficer: Contact = Contact(),
  val offenderManager: ContactWithTelephone = ContactWithTelephone(),
  val probationService: String = "",
  val spoc: Contact = Contact(),
)
