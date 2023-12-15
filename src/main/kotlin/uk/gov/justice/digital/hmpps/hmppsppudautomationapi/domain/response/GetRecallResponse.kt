package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall

data class GetRecallResponse(
  val recall: Recall,
)
