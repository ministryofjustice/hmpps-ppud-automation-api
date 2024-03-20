package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import java.time.LocalDateTime

class CreateRecallRequest(
  val decisionDateTime: LocalDateTime,
  val isExtendedSentence: Boolean,
  val isInCustody: Boolean,
  @field:NotBlank
  val mappaLevel: String,
  @field:NotBlank
  val policeForce: String,
  @field:NotBlank
  val probationArea: String,
  val receivedDateTime: LocalDateTime,
  @field:Valid
  val recommendedTo: PpudUser,
  riskOfContrabandDetails: String? = null,
  @field:NotNull
  val riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel,
) {
  val riskOfContrabandDetails: String = riskOfContrabandDetails ?: ""
}
