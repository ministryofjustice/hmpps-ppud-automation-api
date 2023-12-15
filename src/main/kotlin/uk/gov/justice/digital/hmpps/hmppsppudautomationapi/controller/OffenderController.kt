package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateRecallResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import java.util.*

@RestController
@RequestScope
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class OffenderController(private val ppudClient: PpudClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping("/offender/search")
  suspend fun search(
    @Valid
    @RequestBody(required = true)
    criteria: OffenderSearchRequest,
  ): ResponseEntity<OffenderSearchResponse> {
    log.info("Offender search endpoint hit")
    ensureSearchCriteriaProvided(criteria)
    val results =
      ppudClient.searchForOffender(criteria.croNumber, criteria.nomsId, criteria.familyName, criteria.dateOfBirth)
    return ResponseEntity(OffenderSearchResponse(results), HttpStatus.OK)
  }

  @PostMapping("/offender/{offenderId}/recall")
  suspend fun createRecall(
    @PathVariable(required = true) offenderId: String,
    @Valid
    @RequestBody(required = true)
    createRecallRequest: CreateRecallRequest,
  ): ResponseEntity<CreateRecallResponse> {
    log.info("Offender recall endpoint hit")
    val recall = ppudClient.createRecall(offenderId, createRecallRequest)
    return ResponseEntity(CreateRecallResponse(recall), HttpStatus.CREATED)
  }

  private fun ensureSearchCriteriaProvided(criteria: OffenderSearchRequest) {
    if (!criteria.containsCriteria) {
      throw ValidationException("Valid search criteria must be specified")
    }
  }
}
