package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank

data class AddMinuteRequest(
  @field:NotBlank
  val subject: String,
  @field:NotBlank
  val text: String,
)
