package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import java.util.UUID

data class UploadAdditionalDocumentRequest(
  val documentId: UUID,
  @field:NotBlank
  val title: String,
  @field:Valid
  val owningCaseworker: PpudUser,
)
