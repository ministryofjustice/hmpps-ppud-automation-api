package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class PpudUser(
  @Schema(
    description = "The full name of the user initiating this operation. This is the name as it appears in" +
      " PPUD selectors, e.g. 'Harry Smith', it is not the username.",
  )
  @field:NotBlank
  val fullName: String,

  @Schema(
    description = "The PPUD team name that the user initiating this operation belongs to. It can be seen in" +
      " PPUD selectors in brackets after the user's name. e.g. Harry Smith(Recall Team).",
  )
  @field:NotBlank
  val teamName: String,
) {
  @get:Hidden
  internal val formattedFullNameAndTeam: String
    get() = "${this.fullName}(${this.teamName})"
}
