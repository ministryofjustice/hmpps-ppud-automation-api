package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall

data class CreateRecallResponse(
  val recall: CreatedRecall,
)
