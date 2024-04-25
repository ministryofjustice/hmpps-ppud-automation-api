package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import java.time.LocalDate
import java.time.LocalDateTime

data class Recall(
  val id: String,
  val allMandatoryDocumentsReceived: String,
  val decisionDateTime: LocalDateTime,
  val isInCustody: Boolean,
  val missingMandatoryDocuments: List<DocumentCategory>,
  val nextUalCheck: LocalDate? = null,
  val owningTeam: String,
  val policeForce: String,
  val probationArea: String,
  val recallType: String,
  val receivedDateTime: LocalDateTime,
  val mappaLevel: String,
  val recommendedToOwner: String,
  val recommendedToDateTime: LocalDateTime,
  val revocationIssuedByOwner: String,
  val returnToCustodyNotificationMethod: String,
  val documents: List<Document>,
)
