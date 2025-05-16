package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomInt

fun espPeriod(
  years: Int = randomInt(),
  months: Int = randomInt(),
) = EspPeriod(
  years,
  months,
)
