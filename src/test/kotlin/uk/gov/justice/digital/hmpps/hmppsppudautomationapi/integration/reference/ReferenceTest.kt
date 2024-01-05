@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.reference

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class ReferenceTest : IntegrationTestBase() {

  @ParameterizedTest
  @ValueSource(
    strings = [
      "custody-types",
      "establishments",
      "ethnicities",
      "genders",
      "index-offences",
      "mappa-levels",
      "police-forces",
      "probation-services",
      "released-unders",
    ],
  )
  fun `given missing token when reference endpoints called then unauthorized is returned`(endpoint: String) {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.GET, "/reference/$endpoint")
  }

  @Test
  fun `given missing token when clear-caches called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/reference/clear-caches")
  }

  @Test
  fun `when clear-caches called then OK is returned`() {
    webTestClient.post()
      .uri("/reference/clear-caches")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `when custody-types called then custody types are returned`() {
    webTestClient.get()
      .uri("/reference/custody-types")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Automatic")
      .jsonPath("values.last()").isEqualTo("Unrestricted Patient")
  }

  @Test
  fun `when establishments called then establishments are returned`() {
    webTestClient.get()
      .uri("/reference/establishments")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("1/2 Suttons Drive (test)")
      .jsonPath("values.last()").isEqualTo("zzzztest Establishment")
  }

  @Test
  fun `when ethnicities called then ethnicities are returned`() {
    webTestClient.get()
      .uri("/reference/ethnicities")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Asian or Asian British - Bangladeshi")
      .jsonPath("values.last()").isEqualTo("White – Other")
  }

  @Test
  fun `when genders called then genders are returned`() {
    webTestClient.get()
      .uri("/reference/genders")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("F")
      .jsonPath("values.last()").isEqualTo("M ( Was F )")
  }

  @Test
  fun `when index-offences called then index offences are returned`() {
    webTestClient.get()
      .uri("/reference/index-offences")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Abduction")
      .jsonPath("values.last()")
      .isEqualTo("Wounding with intent to cause grievous bodily harm (section 18 of the Offences against the Person Act 1861)")
  }

  @Test
  fun `when mappa-levels called then mappa levels are returned`() {
    webTestClient.get()
      .uri("/reference/mappa-levels")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Level 1 – Single Agency Management")
      .jsonPath("values.last()").isEqualTo("TB LEVEL 4")
  }

  @Test
  fun `when police-forces called then police forces are returned`() {
    webTestClient.get()
      .uri("/reference/police-forces")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Avon & Somerset Constabulary")
      .jsonPath("values.last()").isEqualTo("Wiltshire Constabulary")
  }

  @Test
  fun `when probation-services called then probation services are returned`() {
    webTestClient.get()
      .uri("/reference/probation-services")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Bedfordshire")
      .jsonPath("values.last()").isEqualTo("Wiltshire")
  }

  @Test
  fun `when released-unders called then released unders are returned`() {
    webTestClient.get()
      .uri("/reference/released-unders")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("CJA 1991")
      .jsonPath("values.last()").isEqualTo("Not Specified")
  }
}
