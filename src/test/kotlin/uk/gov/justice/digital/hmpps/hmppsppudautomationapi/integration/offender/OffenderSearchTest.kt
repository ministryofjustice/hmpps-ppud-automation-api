package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class OffenderSearchTest : IntegrationTestBase() {

  @Test
  fun `given missing request body when search called then bad request is returned`() {
    webTestClient.post()
      .uri("/offender/search")
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `given blank values in request body when search called then bad request is returned`() {
    val requestBody = "{ " +
      "\"croNumber\": \"AA12345\"," +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `given complete set of valid values in request body when search called then ok is returned`() {
    val requestBody = "{ " +
        "\"croNumber\": \"AA12345\"," +
        "\"nomsId\": \"AA12345\"," +
        "\"familyName\": \"AA12345\"," +
        "\"dateOfBirth\": \"1980-12-31\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
  }
}
