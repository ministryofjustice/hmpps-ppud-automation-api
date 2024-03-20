package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

class ContactWithTelephone(
  name: String? = null,
  faxEmail: String? = null,
  telephone: String? = null,
) {
  val name: String = name ?: ""
  val faxEmail: String = faxEmail ?: ""
  val telephone: String = telephone ?: ""
}
