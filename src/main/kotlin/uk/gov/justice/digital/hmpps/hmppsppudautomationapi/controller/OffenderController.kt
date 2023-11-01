package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.OffenderSearchRequest
import java.util.*

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class OffenderController {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping("/offender/search")
  suspend fun search(
    @Valid
    @RequestBody(required = true)
    criteria: OffenderSearchRequest,
  ): ResponseEntity<String> {
    log.info("Offender search endpoint hit")
    ensureSearchCriteriaProvided(criteria)
    return ResponseEntity("", HttpStatus.OK)
  }

  private fun ensureSearchCriteriaProvided(criteria: OffenderSearchRequest) {
    if (!criteria.containsCriteria) {
      throw ValidationException("Valid search criteria must be specified")
    }
  }
}
