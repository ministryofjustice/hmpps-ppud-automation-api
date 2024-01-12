package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_OFFENDER_ID_WITH_NOT_SPECIFIED_RELEASE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudOffenderWithRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId

class OffenderGetTest : IntegrationTestBase() {

  @Test
  fun `given missing token when get offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.GET, "/offender/${randomPpudId()}")
  }

  @Test
  fun `given token without recall role when get offender called then forbidden is returned`() {
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned("/offender/${randomPpudId()}")
  }

  @Test
  fun `given Offender with determinate and indeterminate sentences when get offender called then both sentences are returned`() {
    val id = ppudOffenderWithRelease.id
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

  @Test
  fun `given Offender with release when get offender called then release is returned`() {
    val id = ppudOffenderWithRelease.id
    webTestClient.get()
      .uri("/offender/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isEqualTo("2013-02-02")
      .jsonPath("offender.sentences[0].releases[0].releaseType").isEqualTo("On Licence")
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isEqualTo("CJA 2008")
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isEqualTo("HMP Wakefield")
      .jsonPath("offender.sentences[0].releases[0].category").isEqualTo("A")
      .jsonPath("offender.sentences[1].releases.size()").isEqualTo(0)
  }

  @Test
  fun `given Offender with Not Specified release when get offender called then release is not returned`() {
    val id = PPUD_OFFENDER_ID_WITH_NOT_SPECIFIED_RELEASE
    webTestClient.get()
      .uri("/offender/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("offender.sentences[0].releases.size()").isEqualTo(0)
  }
}
