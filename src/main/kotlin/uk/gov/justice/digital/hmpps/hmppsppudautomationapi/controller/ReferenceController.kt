package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ReferenceResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.ReferenceService
import java.util.*

@RestController
@RequestScope
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class ReferenceController(private val referenceService: ReferenceService) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
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
}
