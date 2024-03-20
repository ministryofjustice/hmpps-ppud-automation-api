package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

class OffenderAddress(
  premises: String? = null,
  line1: String? = null,
  line2: String? = null,
  postcode: String? = null,
  phoneNumber: String? = null,
) {
  val premises: String = premises ?: ""
  val line1: String = line1 ?: ""
  val line2: String = line2 ?: ""
  val postcode: String = postcode ?: ""
  val phoneNumber: String = phoneNumber ?: ""
}
