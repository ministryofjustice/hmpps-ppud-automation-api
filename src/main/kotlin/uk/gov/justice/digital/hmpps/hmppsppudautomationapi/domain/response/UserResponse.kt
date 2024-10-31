package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser

data class UserResponse(
  val results: List<PpudUser> = emptyList(),
)
