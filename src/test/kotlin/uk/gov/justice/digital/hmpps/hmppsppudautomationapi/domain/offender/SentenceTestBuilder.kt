package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

fun sentence(
  id: String = randomString(),
  dateOfSentence: LocalDate = randomDate(),
  custodyType: String = randomString(),
  espCustodialPeriod: EspPeriod? = espPeriod(),
  espExtendedPeriod: EspPeriod? = espPeriod(),
  licenceExpiryDate: LocalDate? = randomDate(),
  mappaLevel: String? = randomString(),
  offence: Offence = offence(),
  releaseDate: LocalDate? = randomDate(),
  sentenceExpiryDate: LocalDate? = randomDate(),
  tariffExpiryDate: LocalDate? = randomDate(),
  sentencedUnder: String? = randomString(),
  sentenceLength: SentenceLength? = sentenceLength(),
  sentencingCourt: String = randomString(),
) = Sentence(
  id,
  dateOfSentence,
  custodyType,
  espCustodialPeriod,
  espExtendedPeriod,
  licenceExpiryDate,
  mappaLevel,
  offence,
  releaseDate,
  sentenceExpiryDate,
  tariffExpiryDate,
  sentencedUnder,
  sentenceLength,
  sentencingCourt,
)
