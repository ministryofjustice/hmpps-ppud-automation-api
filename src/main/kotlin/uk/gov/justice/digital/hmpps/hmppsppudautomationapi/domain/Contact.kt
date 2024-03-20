package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

class Contact(
  name: String? = null,
  faxEmail: String? = null,
) {
  val name: String = name ?: ""
  val faxEmail: String = faxEmail ?: ""
}
