@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.reference

import org.assertj.core.api.Assertions.assertThat
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
      "determinate-custody-types",
      "indeterminate-custody-types",
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

  @ParameterizedTest
  @ValueSource(
    strings = [
      "establishments",
      "ethnicities",
      "index-offences",
      "mappa-levels",
      "police-forces",
      "probation-services",
      "released-unders",
      "determinate-custody-types",
      "indeterminate-custody-types",
    ],
  )
  fun `when reference endpoint called then values are returned and 'not specified' is excluded`(endpoint: String) {
    extractReferenceListValues("/reference/$endpoint")

    assertThat(valuesExtractor.value!!).isNotEmpty
    assertThat(valuesExtractor.value!!).doesNotContain("Not Specified")
  }

  @Test
  fun `when genders called then genders are returned`() {
    extractReferenceListValues("/reference/genders")

    assertThat(valuesExtractor.value!!).isNotEmpty
  }

  private fun extractReferenceListValues(uri: String) {
    webTestClient.get()
      .uri(uri)
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values").value(valuesExtractor)
  }
}
