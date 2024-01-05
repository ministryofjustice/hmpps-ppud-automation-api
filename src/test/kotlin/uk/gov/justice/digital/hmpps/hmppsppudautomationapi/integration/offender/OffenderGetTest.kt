package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId

class OffenderGetTest : IntegrationTestBase() {

  @Test
  fun `given missing token when get offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.GET, "/offender/${randomPpudId()}")
  }

  @Test
  fun `given token without recall role when get offender called then forbidden is returned`() {
    givenTokenWithoutRecallRoleWhenGettingThenForbiddenReturned("/offender/${randomPpudId()}")
  }

  @Test
  fun `given Offender with determinate and indeterminate sentences when get offender called then both sentences are returned`() {
    val id = "4F6666656E64657269643D313632393134G721H665"
    webTestClient.get()
      .uri("/offender/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("offender.sentences[0].custodyType").isEqualTo("Determinate")
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo("2003-06-12")
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo("Level 2 – Local Inter-Agency Management")
      .jsonPath("offender.sentences[1].custodyType").isEqualTo("Indeterminate (life)")
      .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo("2010-09-01")
      .jsonPath("offender.sentences[1].mappaLevel").isEqualTo("")
  }
}
