package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

data class Sentence(
  val id: String,
  val dateOfSentence: LocalDate,
  val custodyType: String,
  val espCustodialPeriod: EspPeriod? = EspPeriod(),
  val espExtendedPeriod: EspPeriod? = EspPeriod(),
  val licenceExpiryDate: LocalDate? = null,
  val mappaLevel: String,
  val offence: Offence = Offence(),
  val releases: List<Release> = emptyList(),
  val sentenceEndDate: LocalDate? = null,
  val sentenceLength: SentenceLength? = SentenceLength(),
  val sentencingCourt: String,
)
