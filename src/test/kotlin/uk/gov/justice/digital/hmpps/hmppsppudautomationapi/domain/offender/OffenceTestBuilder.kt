package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

internal fun offence(
  indexOffence: String = randomString(),
  dateOfIndexOffence: LocalDate? = randomDate(),
) = Offence(indexOffence, dateOfIndexOffence)