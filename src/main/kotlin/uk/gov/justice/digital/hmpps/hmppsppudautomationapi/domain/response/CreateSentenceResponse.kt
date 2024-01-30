package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence

data class CreateSentenceResponse(
  val sentence: CreatedSentence,
)
