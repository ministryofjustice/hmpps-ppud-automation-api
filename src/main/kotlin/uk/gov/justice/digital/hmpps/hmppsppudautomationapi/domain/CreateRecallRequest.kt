package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

class CreateRecallRequest(
  val decisionDateTime: LocalDateTime,
  val isExtendedSentence: Boolean,
  val isInCustody: Boolean,
  @field:NotBlank
  val policeForce: String,
  @field:NotBlank
  val probationArea: String,
  val receivedDateTime: LocalDateTime,
  @field:NotBlank
  val recommendedToOwner: String,
  val releaseDate: LocalDate,
  @field:NotBlank
  val riskOfSeriousHarmLevel: String,
  val sentenceDate: LocalDate,
)
