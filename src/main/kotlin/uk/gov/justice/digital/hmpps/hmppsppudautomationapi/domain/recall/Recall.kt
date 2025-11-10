package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.recall.RecallConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import java.time.LocalDate
import java.time.LocalDateTime

data class Recall(
  val id: String,
  val allMandatoryDocumentsReceived: String,
  val decisionDateTime: LocalDateTime,
  val isInCustody: Boolean,
  val minutes: List<Minute>,
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

/**
 * This enum refers to the recall types supported by CaR when creating recalls.
 * There are more types in PPUD which are ignored/excluded in one way
 * or another (if not directly in this service, in the API or UI services).
 */
enum class SupportedRecallType {
  DETERMINATE_RECALL {
    override fun getFullName(recallConfig: RecallConfig): String = recallConfig.determinateRecallType
  },
  INDETERMINATE_RECALL {
    override fun getFullName(recallConfig: RecallConfig): String = recallConfig.indeterminateRecallType
  }, ;

  abstract fun getFullName(recallConfig: RecallConfig): String
}
