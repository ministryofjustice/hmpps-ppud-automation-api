@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.reference

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class ReferenceTest : IntegrationTestBase() {

  @Test
  fun `when establishments called then establishments are returned`() {
    webTestClient.get()
      .uri("/reference/establishments")
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
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("F")
      .jsonPath("values.last()").isEqualTo("M ( Was F )")
  }
}
