package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.NotBlank

data class PpudUser(
  @field:NotBlank
  val fullName: String,
  @field:NotBlank
  val teamName: String,
) {
  @get:Hidden
  internal val formattedFullNameAndTeam: String
    get() = "${this.fullName}(${this.teamName})"
}
