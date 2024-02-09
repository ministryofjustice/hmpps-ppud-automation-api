package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import java.time.LocalDateTime

data class CreateRecallRequest(
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
  val recommendedTo: PpudUser = PpudUser("", ""),
  @Deprecated(message = "This has now been superseded by recommendedTo so that name and team are passed in separately.")
  @field:Schema(
    deprecated = true,
    description = "The PPUD user who is booking on the recall, in the format <name>(<team name>)",
  )
  val recommendedToOwner: String = "${recommendedTo.fullName}(${recommendedTo.teamName})",
  val riskOfContrabandDetails: String = "",
  @field:NotNull
  val riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel,
)
