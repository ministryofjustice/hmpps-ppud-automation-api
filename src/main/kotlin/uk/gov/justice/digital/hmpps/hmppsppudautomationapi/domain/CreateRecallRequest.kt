package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import java.time.LocalDate

class CreateRecallRequest(
  val sentenceDate: LocalDate,
  val releaseDate: LocalDate,
)
