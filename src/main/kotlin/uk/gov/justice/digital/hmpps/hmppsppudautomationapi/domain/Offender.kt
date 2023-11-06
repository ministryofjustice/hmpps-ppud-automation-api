package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import java.time.LocalDate

data class Offender(
  val id: String,
  val croNumber: String,
  val nomsId: String,
  val firstNames: String,
  val familyName: String,
  val dateOfBirth: LocalDate,
)
