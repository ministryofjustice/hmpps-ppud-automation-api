package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import java.time.LocalDate

// TODO split this up between create and update requests
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
