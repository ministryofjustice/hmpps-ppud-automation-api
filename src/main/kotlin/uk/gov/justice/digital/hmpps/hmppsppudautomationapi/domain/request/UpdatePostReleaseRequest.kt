package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ContactWithTelephone

class UpdatePostReleaseRequest(
  val assistantChiefOfficer: Contact = Contact(),
  val offenderManager: ContactWithTelephone = ContactWithTelephone(),
  probationService: String? = null,
  val spoc: Contact = Contact(),
) {
  val probationService: String = probationService ?: ""
}
