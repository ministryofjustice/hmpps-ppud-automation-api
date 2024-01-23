package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease

data class CreateOrUpdateReleaseResponse(
  val release: CreatedOrUpdatedRelease,
)
