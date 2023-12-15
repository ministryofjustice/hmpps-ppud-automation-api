package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

data class SearchResultOffender(
  val id: String,
  @Deprecated("Replaced with croOtherNumber as CRO isn't specifically identifiable in PPUD")
  val croNumber: String,
  val croOtherNumber: String,
  val dateOfBirth: LocalDate,
  val familyName: String,
  val firstNames: String,
  val nomsId: String,
)
