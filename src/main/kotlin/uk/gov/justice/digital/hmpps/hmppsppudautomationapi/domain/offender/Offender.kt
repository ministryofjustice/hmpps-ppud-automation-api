package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

data class Offender(
  val id: String,
  val croOtherNumber: String,
  val dateOfBirth: LocalDate,
  val ethnicity: String,
  val familyName: String,
  val firstNames: String,
  val gender: String,
  val nomsId: String,
  val sentences: List<Sentence> = listOf(),
)
