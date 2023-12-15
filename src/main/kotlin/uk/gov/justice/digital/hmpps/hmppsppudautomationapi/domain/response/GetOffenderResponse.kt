package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender

data class GetOffenderResponse(
  val offender: Offender,
)
