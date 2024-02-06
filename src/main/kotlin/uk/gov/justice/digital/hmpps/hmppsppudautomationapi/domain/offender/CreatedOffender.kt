package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

data class CreatedOffender(
  val id: String,
  val sentence: CreatedSentence,
)
