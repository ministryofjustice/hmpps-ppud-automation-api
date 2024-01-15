package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class UpdateOffenderRequest(
  val dateOfBirth: LocalDate,
  @field:NotBlank
  val ethnicity: String,
  @field:NotBlank
  val familyName: String,
  @field:NotBlank
  val firstNames: String,
  @field:NotBlank
  val gender: String,
  @field:NotBlank
  val prisonNumber: String,
)
