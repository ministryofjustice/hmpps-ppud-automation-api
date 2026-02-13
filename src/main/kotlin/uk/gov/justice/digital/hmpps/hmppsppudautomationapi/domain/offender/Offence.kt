package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

class Offence(
  val indexOffence: String = "",
  val dateOfIndexOffence: LocalDate? = null,
  val offenceComment: String? = null,
)
