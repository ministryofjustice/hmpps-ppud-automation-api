package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.Pattern
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class OffenderSearchRequest(
  @field:Pattern(regexp = "^\\d{1,6}/\\d{2}[A-Z]$")
  val croNumber: String?,
  @field:Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}$")
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
