package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UserSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response.UserResponse
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient

@RestController
@PreAuthorize("hasRole('ROLE_PPUD_AUTOMATION__RECALL__READWRITE')")
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class UserController(
  private val ppudClient: OperationalPpudClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PostMapping("/user/search")
  @Operation(
    summary = "Search Ppud Users",
    description = "Search for Ppud Users that match the specified criteria.",
  )
  suspend fun search(
    @Valid
    @RequestBody(required = true)
    criteria: UserSearchRequest,
  ): ResponseEntity<UserResponse> {
    log.info("User search endpoint hit")
    ensureSearchCriteriaProvided(criteria)
    val results =
      ppudClient.searchUsers(criteria.fullName, criteria.userName)
    return ResponseEntity(UserResponse(results), HttpStatus.OK)
  }

  @Operation(
    summary = "Retrieve active user records.",
    description = "Retrieve active user records (NB. user Full Name is not unique.)",
  )
  @GetMapping("/user/list")
  suspend fun getActiveUsers(): ResponseEntity<UserResponse> {
    log.info("User get active users endpoint hit")
    val users = ppudClient.retrieveActiveUsers()
    return ResponseEntity(UserResponse(users), HttpStatus.OK)
  }

  private fun ensureSearchCriteriaProvided(criteria: UserSearchRequest) {
    if (!criteria.containsCriteria) {
      throw ValidationException("Valid search criteria must be specified")
    }
  }
}
