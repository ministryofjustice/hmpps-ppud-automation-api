package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class UpdateOffenceRequest(
  @field:NotBlank
  val indexOffence: String,
  val dateOfIndexOffence: LocalDate?,
)
