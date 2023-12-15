package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender

data class OffenderSearchResponse(
  val results: List<SearchResultOffender> = emptyList(),
)
