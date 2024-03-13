@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.reference

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class ReferenceTest : IntegrationTestBase() {

  private val valuesExtractor = ValueConsumer<List<String>>()

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
  @Order(1)
  fun `when clear-caches called then OK is returned`() {
    webTestClient.post()
      .uri("/reference/clear-caches")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `when custody-types called then custody types are returned and only 'Determinate' is included`() {
    // As a temporary measure, we are only handling Determinate, so only returning that
    webTestClient.get()
      .uri("/reference/custody-types")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values.size()").isEqualTo(1)
      .jsonPath("values[0]").isEqualTo("Determinate")
  }

  @Test
  fun `when establishments called then establishments are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/establishments")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("1/2 Suttons Drive (test)")
      .jsonPath("values.last()").isEqualTo("zzzztest Establishment")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }

  @Test
  fun `when ethnicities called then ethnicities are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/ethnicities")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Asian or Asian British - Bangladeshi")
      .jsonPath("values.last()").isEqualTo("White – Other")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
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
  fun `when index-offences called then index offences are returned and 'not specified' is excluded`() {
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
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }

  @Test
  fun `when mappa-levels called then mappa levels are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/mappa-levels")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Level 1 – Single Agency Management")
      .jsonPath("values.last()").isEqualTo("TB LEVEL 4")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }

  @Test
  fun `when police-forces called then police forces are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/police-forces")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Avon & Somerset Constabulary")
      .jsonPath("values.last()").isEqualTo("Wiltshire Constabulary")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }

  @Test
  fun `when probation-services called then probation services are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/probation-services")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Bedfordshire")
      .jsonPath("values.last()").isEqualTo("Wiltshire")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }

  @Test
  fun `when released-unders called then released unders are returned and 'not specified' is excluded`() {
    webTestClient.get()
      .uri("/reference/released-unders")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("CJA 1991")
      .jsonPath("values.last()").isEqualTo("Not Applicable")
      .jsonPath("values").value(valuesExtractor)
    assertFalse(valuesExtractor.value!!.contains("Not Specified"))
  }
}
