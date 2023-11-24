package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import java.time.LocalDate

data class OffenderSearchRequest(
  val croNumber: String?,
  val nomsId: String?,
  val familyName: String?,
  val dateOfBirth: LocalDate?,
) {
  val containsCriteria: Boolean
    get() {
      return !croNumber.isNullOrBlank() ||
        !nomsId.isNullOrBlank() ||
        (!familyName.isNullOrBlank() && dateOfBirth != null)
    }
}
