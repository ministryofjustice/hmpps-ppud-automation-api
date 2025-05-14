package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

fun createdSentence(
  id: String = randomString(),
) = CreatedSentence(id)