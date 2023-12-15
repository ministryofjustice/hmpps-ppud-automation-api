package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender

data class CreateOffenderResponse(
  val offender: CreatedOffender,
)
