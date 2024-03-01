package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.GetRecallResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient

@RestController
@RequestScope
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecallController(private val ppudClient: OperationalPpudClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/recall/{id}")
  suspend fun get(@PathVariable(required = true) id: String): ResponseEntity<GetRecallResponse> {
    log.info("Recall get endpoint hit")
    val recall = ppudClient.retrieveRecall(id)
    return ResponseEntity(GetRecallResponse(recall), HttpStatus.OK)
  }
}
