package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.JwtAuthHelper

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "120000")
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  protected fun HttpHeaders.authToken(roles: List<String> = listOf("ROLE_PPUD_AUTOMATION__RECALL__READWRITE"), subject: String? = "SOME_USER") {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "$subject",
        roles = roles,
      ),
    )
  }

  protected fun givenMissingTokenWhenCalledThenUnauthorizedReturned(method: HttpMethod, uri: String) {
    webTestClient.method(method)
      .uri(uri)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  protected fun givenTokenWithoutRecallRoleWhenPostingThenForbiddenReturned(uri: String, requestBody: String) {
    webTestClient.post()
      .uri(uri)
      .headers { it.authToken(roles = listOf("ANOTHER_ROLE")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isForbidden
  }

  protected fun givenTokenWithoutRecallRoleWhenGettingThenForbiddenReturned(uri: String) {
    webTestClient.get()
      .uri(uri)
      .headers { it.authToken(roles = listOf("ANOTHER_ROLE")) }
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
