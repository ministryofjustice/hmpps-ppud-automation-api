package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateOrUpdateSentenceRequest(
  @field:NotBlank
  @field:Pattern(regexp = "Determinate")
  val custodyType: String,
  val dateOfSentence: LocalDate,
  @field:NotBlank
  val mappaLevel: String,
)
