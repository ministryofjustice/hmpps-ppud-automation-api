package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

class CreateRecallRequest(
  val sentenceDate: LocalDate,
  val releaseDate: LocalDate,
  @field:NotBlank
  val recommendedToOwner: String,
  @field:NotBlank
  val probationArea: String,
  val isInCustody: Boolean = false,
  val decisionDateTime: LocalDateTime,
  val receivedDateTime: LocalDateTime,
  @field:NotBlank
  val policeForce: String,
)
