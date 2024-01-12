package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank

data class UpdateOffenderRequest(
  @field:NotBlank
  val familyName: String,
)
