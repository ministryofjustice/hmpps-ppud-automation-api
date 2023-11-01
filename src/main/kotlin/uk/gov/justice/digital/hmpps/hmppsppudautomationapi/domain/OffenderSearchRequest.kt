package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.NotBlank

data class OffenderSearchRequest(
  @field:NotBlank(message = "CRO Number needed")
  val croNumber: String?,
  @field:NotBlank(message = "nomsId needed")
  val nomsId: String?,
  val familyName: String?,
  val dateOfBirth: String?,
)
