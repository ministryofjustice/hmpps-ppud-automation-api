package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class CreateOrUpdateReleaseRequest(
  val dateOfRelease: LocalDate,
  @field:NotBlank
  val releasedFrom: String,
  @field:NotBlank
  val releasedUnder: String,
)
