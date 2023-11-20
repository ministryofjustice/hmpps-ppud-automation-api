package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

class CreateRecallRequest(
  val sentenceDate: LocalDate,
  val releaseDate: LocalDate,
  @field:NotBlank
  val recommendedToOwner: String,
)
