package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateOrUpdateSentenceRequest(
  @field:NotBlank
  @field:Pattern(regexp = "Determinate")
  val custodyType: String,
)
