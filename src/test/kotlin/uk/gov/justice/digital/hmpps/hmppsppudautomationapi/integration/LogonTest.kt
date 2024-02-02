package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters

@Suppress("SpringBootApplicationProperties")
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["ppud.username=invalid"])
class LogonTest : IntegrationTestBase() {

  @Test
  fun `given invalid credential configuration when logging in to PPUD then 500 internal server error is returned`() {
    val requestBody = """
      {
        "croNumber":"12/12A"    
      }    
    """.trimIndent()

    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("developerMessage")
      .isEqualTo("Logging in to PPUD failed with message 'Invalid credentials, please check and try again.'")
  }
}
