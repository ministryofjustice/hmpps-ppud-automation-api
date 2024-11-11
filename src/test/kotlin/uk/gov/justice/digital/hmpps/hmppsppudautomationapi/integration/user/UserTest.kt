@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.user

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UserSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class UserTest : IntegrationTestBase() {

  @Test
  fun `given missing token when get active users called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.GET, "/user/list")
  }

  @Test
  fun `given missing token when search active users called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/user/search")
  }

  @Test
  @Order(1)
  fun `when get active users called then OK is returned`() {
    webTestClient.get()
      .uri("/user/list")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `when get active users called then users are returned`() {
    val valuesExtractor = ValueConsumer<List<LinkedHashMap<String, String>>>()

    webTestClient.get()
      .uri("/user/list")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results[0].fullName").isEqualTo("aaaaaaaa")
      .jsonPath("results").value(valuesExtractor)

    assert(valuesExtractor.value != null)
    assert(valuesExtractor.value!!.isNotEmpty())
    assert(valuesExtractor.value!!.last().lastEntry().value == PpudUser("zzzzzFricker, Joanne", "Performance Management").formattedFullNameAndTeam)
  }

  @Test
  fun `when search active users for username 'car_test_admin' called then single user is returned`() {
    webTestClient.post()
      .uri("/user/search")
      .headers { it.authToken() }
      .header(HttpHeaders.CONTENT_TYPE, "application/json")
      .body(BodyInserters.fromValue(UserSearchRequest("", "car_test_admin")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results.length()").isEqualTo(1)
      .jsonPath("results[0].fullName").isEqualTo("Consider a Recall Test Admin")
      .jsonPath("results[0].teamName").isEqualTo("Performance Management")
  }

  @Test
  fun `when search active users for user fullName 'car_test' called then 2 users are returned`() {
    webTestClient.post()
      .uri("/user/search")
      .headers { it.authToken() }
      .header(HttpHeaders.CONTENT_TYPE, "application/json")
      .body(BodyInserters.fromValue(UserSearchRequest("Consider a Recall Test", "")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results.length()").isEqualTo(2)
      .jsonPath("results[0].fullName").isEqualTo("Consider a Recall Test")
      .jsonPath("results[0].teamName").isEqualTo("Recall 1")
  }
}
