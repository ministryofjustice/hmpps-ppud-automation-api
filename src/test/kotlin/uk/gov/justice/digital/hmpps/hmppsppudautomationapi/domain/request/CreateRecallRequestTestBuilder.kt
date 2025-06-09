package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomBoolean
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDateTime
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDateTime

fun createRecallRequest(
  decisionDateTime: LocalDateTime = randomDateTime(),
  isExtendedSentence: Boolean = randomBoolean(),
  isInCustody: Boolean = randomBoolean(),
  mappaLevel: String = randomString(),
  policeForce: String = randomString(),
  probationArea: String = randomString(),
  receivedDateTime: LocalDateTime = randomDateTime(),
  recommendedTo: PpudUser = generatePpudUser(),
  riskOfContrabandDetails: String? = randomString(),
): CreateRecallRequest = CreateRecallRequest(
  decisionDateTime,
  isExtendedSentence,
  isInCustody,
  mappaLevel,
  policeForce,
  probationArea,
  receivedDateTime,
  recommendedTo,
  riskOfContrabandDetails,
)