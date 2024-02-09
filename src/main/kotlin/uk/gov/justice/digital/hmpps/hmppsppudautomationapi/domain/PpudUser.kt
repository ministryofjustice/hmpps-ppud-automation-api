package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.NotBlank

data class PpudUser(
  @field:NotBlank
  val fullName: String,
  @field:NotBlank
  val teamName: String,
)
