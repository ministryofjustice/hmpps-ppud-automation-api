package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength

internal class TestOffender(
  val id: String,
  val dateOfBirth: String,
  val familyName: String,
  val firstNames: String,
  val prisonNumber: String,
  val determinateSentence: DeterminateTestSentence,
  val indeterminateSentence: IndeterminateTestSentence,
)

class TestOffence(
  val indexOffence: String,
  val dateOfIndexOffence: String? = null,
)

abstract class TestSentence(
  val custodyType: String,
  val expiryDate: String,
  val releaseDate: String,
  val sentenceDate: String,
  val sentenceLength: SentenceLength,
  val sentencingCourt: String,
  val offence: TestOffence,
)

internal class DeterminateTestSentence(
  custodyType: String,
  expiryDate: String,
  releaseDate: String,
  sentenceDate: String,
  sentenceLength: SentenceLength,
  sentencingCourt: String,
  offence: TestOffence,
  val espCustodialPeriod: EspPeriod,
  val espExtendedPeriod: EspPeriod,
  val licenseExpiryDate: String,
  val mappaLevel: String,
  val sentencedUnder: String,
) : TestSentence(
  custodyType,
  expiryDate,
  releaseDate,
  sentenceDate,
  sentenceLength,
  sentencingCourt,
  offence,
)

internal class IndeterminateTestSentence(
  custodyType: String,
  expiryDate: String,
  releaseDate: String,
  sentenceDate: String,
  sentenceLength: SentenceLength,
  sentencingCourt: String,
  offence: TestOffence,
) : TestSentence(
  custodyType,
  expiryDate,
  releaseDate,
  sentenceDate,
  sentenceLength,
  sentencingCourt,
  offence,
)
