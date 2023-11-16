package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import java.time.LocalDate

class OffenderRecallRequest(
  val sentenceDate: LocalDate,
  val releaseDate: LocalDate,
)
