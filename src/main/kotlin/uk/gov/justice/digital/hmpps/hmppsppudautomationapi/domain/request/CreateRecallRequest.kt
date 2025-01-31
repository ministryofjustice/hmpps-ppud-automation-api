package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
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
) {
  val riskOfContrabandDetails: String = riskOfContrabandDetails ?: ""
}
