package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UploadAdditionalDocumentRequest(
  val documentId: UUID,
  @field:NotBlank
  val title: String,
)
