package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateOffenderResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateRecallResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.CreateSentenceResponse
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
  @Operation(
    summary = "Search Offenders",
    description = "Search for offenders that match the specified criteria.",
  )
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
  @Operation(
    summary = "Get Offender",
    description = "Retrieve data for a specific offender.",
  )
  suspend fun get(
    @PathVariable(required = true) id: String,
    @Parameter(
      description = "Only required for testing/diagnostic purposes. If true, this will mean that releases " +
        "that are titles as 'Not Specified - Not Specified' will be included.",
    )
    @RequestParam(required = false) includeEmptyReleases: Boolean = false,
  ): ResponseEntity<GetOffenderResponse> {
    log.info("Offender get endpoint hit")
    val offender = ppudClient.retrieveOffender(id, includeEmptyReleases)
    return ResponseEntity(GetOffenderResponse(offender), HttpStatus.OK)
  }

  @PostMapping("/offender")
  @Operation(
    summary = "Create Offender",
    description = "Create a new offender.",
  )
  suspend fun createOffender(
    @Valid
    @RequestBody(required = true)
    createOffenderRequest: CreateOffenderRequest,
  ): ResponseEntity<CreateOffenderResponse> {
    log.info("Offender creation endpoint hit")
    val offender = ppudClient.createOffender(createOffenderRequest)
    return ResponseEntity(CreateOffenderResponse(offender), HttpStatus.CREATED)
  }

  @PutMapping("/offender/{offenderId}")
  @Operation(
    summary = "Update Offender",
    description = "Update an existing offender.",
  )
  suspend fun updateOffender(
    @PathVariable(required = true) offenderId: String,
    @Valid
    @RequestBody(required = true)
    offenderRequest: UpdateOffenderRequest,
  ) {
    log.info("Offender update endpoint hit")
    ppudClient.updateOffender(offenderId, offenderRequest)
  }

  @PostMapping("/offender/{offenderId}/sentence")
  @Operation(
    summary = "Create Sentence",
    description = "Create a new sentence against an existing offender.",
  )
  suspend fun createSentence(
    @PathVariable(required = true) offenderId: String,
    @Valid
    @RequestBody(required = true)
    createOrUpdateSentenceRequest: CreateOrUpdateSentenceRequest,
  ): ResponseEntity<CreateSentenceResponse> {
    log.info("Sentence create endpoint hit")
    val sentence = ppudClient.createSentence(offenderId, createOrUpdateSentenceRequest)
    return ResponseEntity(CreateSentenceResponse(sentence), HttpStatus.CREATED)
  }

  @Operation(
    summary = "Update Sentence",
    description = "Update an existing sentence on an offender.",
  )
  @PutMapping("/offender/{offenderId}/sentence/{sentenceId}")
  suspend fun updateSentence(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @Valid
    @RequestBody(required = true)
    createOrUpdateSentenceRequest: CreateOrUpdateSentenceRequest,
  ) {
    log.info("Sentence update endpoint hit")
    ppudClient.updateSentence(offenderId, sentenceId, createOrUpdateSentenceRequest)
  }

  @Operation(
    summary = "Update Offence",
    description = "Update an existing offence associated with a sentence on an offender.",
  )
  @PutMapping("/offender/{offenderId}/sentence/{sentenceId}/offence")
  suspend fun updateOffence(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @Valid
    @RequestBody(required = true)
    request: UpdateOffenceRequest,
  ) {
    log.info("Offence update endpoint hit")
    ppudClient.updateOffence(offenderId, sentenceId, request)
  }

  @Operation(
    summary = "Create or Update a Release and Post Release",
    description = "Create a new release and post release, or update an existing release and post release when the release matches the key values.",
  )
  @PostMapping("/offender/{offenderId}/sentence/{sentenceId}/release")
  suspend fun createOrUpdateRelease(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @Valid
    @RequestBody(required = true)
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ): ResponseEntity<CreateOrUpdateReleaseResponse> {
    log.info("Release create or update endpoint hit")
    val createdOrUpdatedRelease = ppudClient.createOrUpdateRelease(offenderId, sentenceId, createOrUpdateReleaseRequest)
    return ResponseEntity(CreateOrUpdateReleaseResponse(createdOrUpdatedRelease), HttpStatus.OK)
  }

  @PostMapping("/offender/{offenderId}/recall")
  @Operation(
    summary = "Create Recall",
    description = "Create a recall against an existing offender.",
  )
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
