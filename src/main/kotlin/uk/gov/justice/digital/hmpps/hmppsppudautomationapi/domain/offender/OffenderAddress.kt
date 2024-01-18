package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

data class OffenderAddress(
  val premises: String = "",
  val line1: String = "",
  val line2: String = "",
  val postcode: String = "",
  val phoneNumber: String = "",
)
