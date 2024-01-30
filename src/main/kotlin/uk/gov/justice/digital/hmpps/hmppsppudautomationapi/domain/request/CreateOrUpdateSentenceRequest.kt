package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import java.time.LocalDate

data class CreateOrUpdateSentenceRequest(
  @field:NotBlank
  @field:Pattern(regexp = "Determinate")
  val custodyType: String,
  val dateOfSentence: LocalDate,
  val licenceExpiryDate: LocalDate?,
  @field:NotBlank
  val mappaLevel: String,
  val releaseDate: LocalDate?,
  val sentenceLength: SentenceLength?,
  val espCustodialPeriod: EspPeriod?,
  val espExtendedPeriod: EspPeriod?,
  val sentenceExpiryDate: LocalDate?,
  @field:Size(min = 0, max = 50)
  val sentencingCourt: String = "",
)
