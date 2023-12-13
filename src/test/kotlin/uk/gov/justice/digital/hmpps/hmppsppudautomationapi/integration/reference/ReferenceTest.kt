@file:Suppress("JsonPathUnknownFunction")

package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.reference

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase

class ReferenceTest : IntegrationTestBase() {

  @Test
  fun `when custody-types called then custody types are returned`() {
    webTestClient.get()
      .uri("/reference/custody-types")
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

  @Test
  fun `when index-offences called then index offences are returned`() {
    webTestClient.get()
      .uri("/reference/index-offences")
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
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("values[0]").isEqualTo("Level 1 – Single Agency Management")
      .jsonPath("values.last()").isEqualTo("TB LEVEL 4")
  }
}
