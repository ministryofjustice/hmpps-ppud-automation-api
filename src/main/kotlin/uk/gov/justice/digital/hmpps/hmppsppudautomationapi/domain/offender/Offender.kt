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
  val immigrationStatus: String,
  val nomsId: String,
  val prisonerCategory: String,
  val prisonNumber: String,
  val sentences: List<Sentence> = listOf(),
  val status: String,
  val youngOffender: String,
)
