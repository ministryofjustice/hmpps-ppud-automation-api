package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

data class OffenderSearchResponse(
  val results: List<Offender> = emptyList(),
)
