package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.GetRecallResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.DocumentService

@RestController
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecallController(
  private val documentService: DocumentService,
  private val ppudClient: OperationalPpudClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(
    summary = "Retrieve a specific recall.",
    description = "Retrieve a recall identified by the specified recall ID.",
  )
  @GetMapping("/recall/{id}")
  suspend fun get(@PathVariable(required = true) id: String): ResponseEntity<GetRecallResponse> {
    log.info("Recall get endpoint hit")
    val recall = ppudClient.retrieveRecall(id)
    return ResponseEntity(GetRecallResponse(recall), HttpStatus.OK)
  }

  @Operation(
    summary = "Upload a mandatory document to a recall.",
    description = "Add a document of the specified category to the recall identified by the recallId.  The document" +
      "must be present in the Document Management API and identified by the supplied documentId.",
  )
  @PutMapping("/recall/{recallId}/mandatory-document")
  suspend fun uploadMandatoryDocument(
    @PathVariable(required = true) recallId: String,
    @Valid
    @RequestBody(required = true)
    request: UploadMandatoryDocumentRequest,
  ) {
    log.info("Recall mandatory document upload endpoint hit")
    val path = documentService.downloadDocument(request.documentId)
    try {
      ppudClient.uploadMandatoryDocument(recallId, request, path)
    } finally {
      documentService.deleteDownloadedDocument(path)
    }
  }

  @Operation(
    summary = "Upload an additional document to a recall.",
    description = "Add a non-mandatory document to the recall identified by the recallId.  The document" +
      "must be present in the Document Management API and identified by the supplied documentId.",
  )
  @PutMapping("/recall/{recallId}/additional-document")
  suspend fun uploadAdditionalDocument(
    @PathVariable(required = true) recallId: String,
    @Valid
    @RequestBody(required = true)
    request: UploadAdditionalDocumentRequest,
  ) {
    log.info("Recall additional document upload endpoint hit")
    val path = documentService.downloadDocument(request.documentId)
    try {
      ppudClient.uploadAdditionalDocument(recallId, request, path)
    } finally {
      documentService.deleteDownloadedDocument(path)
    }
  }
}
