package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class Recall(
  val id: String,
  val allMandatoryDocumentsReceived: String,
  val decisionDateTime: LocalDateTime,
  val isInCustody: Boolean,
  val mappaLevel: String,
  val nextUalCheck: LocalDate? = null,
  val owningTeam: String,
  val policeForce: String,
  val probationArea: String,
  val recallType: String,
  val receivedDateTime: LocalDateTime,
  val recommendedToDateTime: LocalDateTime,
  val recommendedToOwner: String,
  val returnToCustodyNotificationMethod: String,
  val revocationIssuedByOwner: String,
)
