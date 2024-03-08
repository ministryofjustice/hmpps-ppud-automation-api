package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Hidden
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// TODO: Delete this when finished testing ingress timeouts
@RestController
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class TimeoutController {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping("/timeout/{seconds}")
  @Hidden
  suspend fun timeoutTestPost(@PathVariable(required = true) seconds: Long): ResponseEntity<String> {
    log.info("Post timeout endpoint hit")
    delay(seconds * 1000)
    log.info("Post delay completed")
    return ResponseEntity("Done", HttpStatus.OK)
  }

  @PutMapping("/timeout/{seconds}")
  @Hidden
  suspend fun timeoutTestPut(@PathVariable(required = true) seconds: Long): ResponseEntity<String> {
    log.info("Put timeout endpoint hit")
    delay(seconds * 1000)
    log.info("Put delay completed")
    return ResponseEntity("Done", HttpStatus.OK)
  }

  @GetMapping("/timeout/{seconds}")
  @Hidden
  suspend fun timeoutTestGet(@PathVariable(required = true) seconds: Long): ResponseEntity<String> {
    log.info("Get timeout endpoint hit")
    delay(seconds * 1000)
    log.info("Get delay completed")
    return ResponseEntity("Done", HttpStatus.OK)
  }
}
