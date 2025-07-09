package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.ReferenceResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.ReferenceService

@RestController
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class ReferenceController(private val referenceService: ReferenceService) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping("/reference/clear-caches")
  @Operation(
    summary = "Clear cached reference data",
    description = "Clear any cached reference data, meaning that the service will be forced to retrieve" +
      " values from PPUD when the next reference data request is made.",
  )
  fun clearCaches() {
    log.info("Reference data clear-caches endpoint hit")
    referenceService.clearCaches()
  }

  @PostMapping("/reference/refresh-caches")
  @Operation(
    summary = "Refresh cached reference data",
    description = "Retrieve the latest reference data values from PPUD and update the cache.",
  )
  suspend fun refreshCaches() {
    log.info("Reference data refresh-caches endpoint hit")
    referenceService.refreshCaches()
  }

  @Deprecated("replaced by separate determinate and indeterminate endpoints")
  @GetMapping("/reference/custody-types")
  suspend fun custodyTypes(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data custody-types endpoint hit")
    val values = referenceService.retrieveCustodyTypes()
      .filter { it == "Determinate" } // Temporary until we handle other types of recall
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/establishments")
  suspend fun establishments(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data establishments endpoint hit")
    val values = referenceService.retrieveEstablishments()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/ethnicities")
  suspend fun ethnicities(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data ethnicities endpoint hit")
    val values = referenceService.retrieveEthnicities()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/genders")
  suspend fun genders(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data genders endpoint hit")
    val values = referenceService.retrieveGenders()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/index-offences")
  suspend fun indexOffences(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data index offences endpoint hit")
    val values = referenceService.retrieveIndexOffences()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/mappa-levels")
  suspend fun mappaLevels(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data mappa levels endpoint hit")
    val values = referenceService.retrieveMappaLevels()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/police-forces")
  suspend fun policeForces(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data police forces endpoint hit")
    val values = referenceService.retrievePoliceForces()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/probation-services")
  suspend fun probationServices(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data probation services endpoint hit")
    val values = referenceService.retrieveProbationServices()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/released-unders")
  suspend fun releaseUnders(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data released unders endpoint hit")
    val values = referenceService.retrieveReleasedUnders()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/determinate-custody-types")
  suspend fun determinateCustodyTypes(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data determinate custody types endpoint hit")
    val values = referenceService.retrieveDeterminateCustodyTypes()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }

  @GetMapping("/reference/indeterminate-custody-types")
  suspend fun indeterminateCustodyTypes(): ResponseEntity<ReferenceResponse> {
    log.info("Reference data indeterminate custody types endpoint hit")
    val values = referenceService.retrieveIndeterminateCustodyTypes()
    return ResponseEntity(ReferenceResponse(values), HttpStatus.OK)
  }
}
