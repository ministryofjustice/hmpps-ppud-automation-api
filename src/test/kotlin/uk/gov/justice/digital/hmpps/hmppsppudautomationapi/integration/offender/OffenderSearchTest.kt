package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.util.function.Consumer

class OffenderSearchTest : IntegrationTestBase() {

  @Test
  fun `given missing token when search called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/offender/search")
  }

  @Test
  fun `given token without recall role when search called then forbidden is returned`() {
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned("/offender/search", "{}")
  }

  @Test
  fun `given missing request body when search called then bad request is returned`() {
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `given blank values in request body when search called then bad request is returned`() {
    val requestBody = "{}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Valid search criteria must be specified") })
  }

  @Test
  fun `given complete set of valid values in request body when search called then ok is returned`() {
    val requestBody = "{ " +
      "\"croNumber\": \"12/12A\"," +
      "\"nomsId\": \"B1234XX\"," +
      "\"familyName\": \"Smith\"," +
      "\"dateOfBirth\": \"1980-12-31\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "0001-01-01",
      "2005-01-01",
      "2005-12-31",
    ],
  )
  fun `given valid dateOfBirth in request body when search called then request is accepted`(dateOfBirth: String) {
    val requestBody = "{ " +
      "\"familyName\": \"Test\"," +
      "\"dateOfBirth\": \"$dateOfBirth\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "0",
      "1 Jan 2003",
      "1/1/2003",
      "01/01/2003",
      "01-01-2003",
      "2000-13-01",
      "2000-12-32",
    ],
  )
  fun `given invalid dateOfBirth in request body when search called then bad request is returned`(dateOfBirth: String) {
    val requestBody = "{ " +
      "\"familyName\": \"Test\"," +
      "\"dateOfBirth\": \"$dateOfBirth\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Cannot deserialize value of type `java.time.LocalDate`") })
  }

  @Test
  fun `given CRO number of existing offender when search called then offender details are returned`() {
    val croNumber = "5159/08A"
    val firstNames = "John"
    val familyName = "Teal"
    val id = "4F6666656E64657269643D313731383134G721H664"
    val requestBody = "{ " +
      "\"croNumber\": \"$croNumber\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results[0].id").isEqualTo(id)
      .jsonPath("results[0].croNumber").isEqualTo(croNumber)
      .jsonPath("results[0].firstNames").isEqualTo(firstNames)
      .jsonPath("results[0].familyName").isEqualTo(familyName)
  }

  @Test
  fun `given NOMS ID of existing offender when search called then offender details are returned`() {
    val nomsId = "A1122BC"
    val firstNames = "Gary"
    val familyName = "Teal"
    val id = "4F6666656E64657269643D313731383135G722H664"
    val requestBody = "{ " +
      "\"nomsId\": \"$nomsId\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results[0].id").isEqualTo(id)
      .jsonPath("results[0].nomsId").isEqualTo(nomsId)
      .jsonPath("results[0].firstNames").isEqualTo(firstNames)
      .jsonPath("results[0].familyName").isEqualTo(familyName)
  }

  @Test
  fun `given family name and date of birth of existing offender when search called then offender details are returned`() {
    val dateOfBirth = LocalDate.parse("1902-12-12")
    val firstNames = "Duncan"
    val familyName = "Teal"
    val id = "4F6666656E64657269643D313731383136G723H664"
    val requestBody = "{ " +
      "\"familyName\": \"$familyName\"," +
      "\"dateOfBirth\": \"$dateOfBirth\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results[0].id").isEqualTo(id)
      .jsonPath("results[0].firstNames").isEqualTo(firstNames)
      .jsonPath("results[0].familyName").isEqualTo(familyName)
      .jsonPath("results[0].dateOfBirth").isEqualTo(dateOfBirth.toString())
  }

  @Test
  fun `given family name and date of birth of multiple existing offenders when search called then offender details are returned`() {
    val dateOfBirth = LocalDate.parse("1980-12-31")
    val familyName = "Teal"
    val requestBody = "{ " +
      "\"familyName\": \"$familyName\"," +
      "\"dateOfBirth\": \"$dateOfBirth\"" +
      "}"
    webTestClient.post()
      .uri("/offender/search")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("results[0].id").isEqualTo("4F6666656E64657269643D313731383138G725H664")
      .jsonPath("results[0].firstNames").isEqualTo("Jane")
      .jsonPath("results[0].familyName").isEqualTo(familyName)
      .jsonPath("results[0].dateOfBirth").isEqualTo(dateOfBirth.toString())
      .jsonPath("results[1].id").isEqualTo("4F6666656E64657269643D313731383139G726H664")
      .jsonPath("results[1].firstNames").isEqualTo("Emily")
      .jsonPath("results[1].familyName").isEqualTo(familyName)
      .jsonPath("results[1].dateOfBirth").isEqualTo(dateOfBirth.toString())
  }
}
