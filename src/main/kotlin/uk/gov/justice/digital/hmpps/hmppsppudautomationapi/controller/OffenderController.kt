package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateOffenderResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateRecallResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.GetOffenderResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestScope
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
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

  @GetMapping("/offender/{id}")
  suspend fun get(@PathVariable(required = true) id: String): ResponseEntity<GetOffenderResponse> {
    log.info("Offender get endpoint hit")
    val offender = ppudClient.retrieveOffender(id)
    return ResponseEntity(GetOffenderResponse(offender), HttpStatus.OK)
  }

  @PostMapping("/offender")
  suspend fun createOffender(
    @Valid
    @RequestBody(required = true)
    createOffenderRequest: CreateOffenderRequest,
  ): ResponseEntity<CreateOffenderResponse> {
    log.info("Offender creation endpoint hit")
    val offender = ppudClient.createOffender(createOffenderRequest)
    return ResponseEntity(CreateOffenderResponse(offender), HttpStatus.CREATED)
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

  @Hidden
  @PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__TESTS__READWRITE')")
  @DeleteMapping("/offender")
  suspend fun deleteTestOffenders(
    @RequestParam(required = true) familyNamePrefix: String,
    @RequestParam(required = true) testRunId: UUID,
  ) {
    log.info("Offender deletion endpoint hit")
    ppudClient.deleteOffenders(familyName = "$familyNamePrefix-$testRunId")
  }

  @Hidden
  @PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__TESTS__READWRITE')")
  @DeleteMapping("/offender/{offenderId}/recalls")
  suspend fun deleteRecalls(
    @PathVariable(required = true) offenderId: String,
    @RequestParam(required = true) sentenceDate: LocalDate,
    @RequestParam(required = true) releaseDate: LocalDate,
  ) {
    log.info("Offender recall deletion endpoint hit")
    ppudClient.deleteRecalls(offenderId, sentenceDate, releaseDate)
  }

  private fun ensureSearchCriteriaProvided(criteria: OffenderSearchRequest) {
    if (!criteria.containsCriteria) {
      throw ValidationException("Valid search criteria must be specified")
    }
  }
}
