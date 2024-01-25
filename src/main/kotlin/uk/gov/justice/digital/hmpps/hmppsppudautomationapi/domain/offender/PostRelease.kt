package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ContactWithTelephone

data class PostRelease(
  val assistantChiefOfficer: Contact = Contact(),
  val licenceType: String = "",
  val offenderManager: ContactWithTelephone = ContactWithTelephone(),
  val probationService: String = "",
  val spoc: Contact = Contact(),
)
