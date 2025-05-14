package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.espPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.sentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

fun createOrUpdateSentenceRequest(
  custodyType: String = randomString(),
  dateOfSentence: LocalDate = randomDate(),
  licenceExpiryDate: LocalDate? = randomDate(),
  mappaLevel: String = randomString(),
  releaseDate: LocalDate? = randomDate(),
  sentenceLength: SentenceLength? = sentenceLength(),
  espCustodialPeriod: EspPeriod? = espPeriod(),
  espExtendedPeriod: EspPeriod? = espPeriod(),
  sentenceExpiryDate: LocalDate? = randomDate(),
  sentencingCourt: String = randomString(),
  sentencedUnder: String = randomString(),
) = CreateOrUpdateSentenceRequest(
  custodyType,
  dateOfSentence,
  licenceExpiryDate,
  mappaLevel,
  releaseDate,
  sentenceLength,
  espCustodialPeriod,
  espExtendedPeriod,
  sentenceExpiryDate,
  sentencingCourt,
  sentencedUnder,
)