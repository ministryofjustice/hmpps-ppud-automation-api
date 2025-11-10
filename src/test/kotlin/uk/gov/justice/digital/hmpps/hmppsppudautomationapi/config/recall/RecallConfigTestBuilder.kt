package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.recall

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

fun recallConfig(
  determinateRecallType: String = randomString(),
  indeterminateRecallType: String = randomString(),
): RecallConfig = RecallConfig(determinateRecallType, indeterminateRecallType)
