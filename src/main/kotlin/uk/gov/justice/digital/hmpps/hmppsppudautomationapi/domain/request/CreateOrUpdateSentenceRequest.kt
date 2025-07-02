package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import java.time.LocalDate

data class CreateOrUpdateSentenceRequest(
  val custodyType: String,
  val dateOfSentence: LocalDate,
  val licenceExpiryDate: LocalDate?,
  val mappaLevel: String?,
  val releaseDate: LocalDate?,
  val sentenceLength: SentenceLength?,
  val espCustodialPeriod: EspPeriod?, // never set in UI - should we remove it and all associated code?
  val espExtendedPeriod: EspPeriod?, // never set in UI - should we remove it and all associated code?
  val sentenceExpiryDate: LocalDate?,
  val sentencingCourt: String = "",
  val sentencedUnder: String?,
)
