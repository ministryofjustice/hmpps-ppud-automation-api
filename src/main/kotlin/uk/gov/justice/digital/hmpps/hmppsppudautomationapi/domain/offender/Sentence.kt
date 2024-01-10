package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

data class Sentence(
  val dateOfSentence: LocalDate,
  val custodyType: String,
  val mappaLevel: String,
  val releases: List<Release> = emptyList(),
)
