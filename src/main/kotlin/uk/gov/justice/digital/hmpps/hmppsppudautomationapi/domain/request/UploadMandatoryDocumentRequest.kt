package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import java.util.UUID

data class UploadMandatoryDocumentRequest(
  val documentId: UUID,
  val category: DocumentCategory,
  @field:Valid
  val owningCaseworker: PpudUser,
)
