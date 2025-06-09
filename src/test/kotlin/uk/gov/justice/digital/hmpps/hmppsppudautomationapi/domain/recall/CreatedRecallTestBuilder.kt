package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

fun createdRecall(
  id: String = randomString()
): CreatedRecall = CreatedRecall(id)