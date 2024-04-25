package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import java.util.UUID

data class UploadMandatoryDocumentRequest(
  val documentId: UUID,
  val category: DocumentCategory,
)
