package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomInt

fun sentenceLength(
  partYears: Int = randomInt(),
  partMonths: Int = randomInt(),
  partDays: Int = randomInt(),
) = SentenceLength(
  partYears,
  partMonths,
  partDays,
)