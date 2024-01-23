package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_FROM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_FROM_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_UNDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_UNDER_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudOffenderWithRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderReleaseTest.OffenderReleaseDataTidyExtension::class)
class OffenderReleaseTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("dateOfRelease", releaseRequestBody(dateOfRelease = "")),
        MandatoryFieldTestData("releasedFrom", releaseRequestBody(releasedFrom = "")),
        MandatoryFieldTestData("releasedUnder", releaseRequestBody(releasedUnder = "")),
      )
    }

    fun releaseRequestBody(
      dateOfRelease: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      releasedFrom: String = PPUD_VALID_RELEASED_FROM,
      releasedUnder: String = PPUD_VALID_RELEASED_UNDER,
    ): String {
      return "{" +
        "\"dateOfRelease\":\"$dateOfRelease\", " +
        "\"releasedFrom\":\"$releasedFrom\", " +
        "\"releasedUnder\":\"$releasedUnder\" " +
        "}"
    }
  }

  internal class OffenderReleaseDataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing request body when post release called then bad request is returned`() {
    webTestClient.post()
      .uri(constructUri(randomPpudId(), randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `given invalid offender ID when post release called then bad request is returned`() {
    val requestBody = releaseRequestBody()
    postRelease(offenderId = randomPpudId(), sentenceId = randomPpudId(), requestBody = requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Offender ID is invalid") })
  }

  @Test
  fun `given sentence ID that does not exist on the offender when post release called then not found is returned`() {
    val requestBody = releaseRequestBody()
    postRelease(offenderId = ppudOffenderWithRelease.id, sentenceId = randomPpudId(), requestBody = requestBody)
      .expectStatus()
      .isNotFound
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Sentence was not found") })
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when post release called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    postRelease(offenderId = randomPpudId(), sentenceId = randomPpudId(), requestBody = data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing token when post release called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, constructUri(randomPpudId(), randomPpudId()))
  }

  @Test
  fun `given token without recall role when post release called then forbidden is returned`() {
    val requestBody = releaseRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      constructUri(randomPpudId(), randomPpudId()),
      requestBody,
      HttpMethod.POST,
    )
  }

  @Test
  fun `given existing release and valid values in request body when post release called then category and release type are set`() {
    // TODO: This test worked OK to drive the initial work, but we need to be able to create an offender
    //  with a release that has different values for category and release type.
    val testOffenderId = ppudOffenderWithRelease.id
    val idExtractor = ValueConsumer<String>()
    val dateOfReleaseExtractor = ValueConsumer<String>()
    val releasedUnderExtractor = ValueConsumer<String>()
    val releasedFromExtractor = ValueConsumer<String>()
    retrieveOffender(testOffenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isNotEmpty
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").value(dateOfReleaseExtractor)
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isNotEmpty
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").value(releasedUnderExtractor)
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isNotEmpty
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").value(releasedFromExtractor)
    val sentenceId = idExtractor.value!!
    val dateOfRelease = dateOfReleaseExtractor.value!!
    val releasedUnder = releasedUnderExtractor.value!!
    val releasedFrom = releasedFromExtractor.value!!
    val requestBody = releaseRequestBody(
      dateOfRelease = dateOfRelease,
      releasedFrom = releasedFrom,
      releasedUnder = releasedUnder,
    )

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("release.id").isNotEmpty

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isEqualTo(dateOfRelease)
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isEqualTo(releasedFrom)
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isEqualTo(releasedUnder)
      .jsonPath("offender.sentences[0].releases[0].releaseType").isEqualTo("On Licence")
      .jsonPath("offender.sentences[0].releases[0].category").isEqualTo("Not Applicable")
  }

  @Test
  fun `given new release and valid values in request body when post release called then release is created`() {
    val testOffenderId = createTestOffenderInPpud()
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(testOffenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!
    val dateOfRelease = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val releasedFrom = PPUD_VALID_RELEASED_FROM_2
    val releasedUnder = PPUD_VALID_RELEASED_UNDER_2
    val requestBody = releaseRequestBody(
      dateOfRelease = dateOfRelease,
      releasedFrom = releasedFrom,
      releasedUnder = releasedUnder,
    )

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("release.id").isNotEmpty

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isEqualTo(dateOfRelease)
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isEqualTo(releasedFrom)
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isEqualTo(releasedUnder)
      .jsonPath("offender.sentences[0].releases[0].releaseType").isEqualTo("On Licence")
      .jsonPath("offender.sentences[0].releases[0].category").isEqualTo("Not Applicable")
  }

  private fun postRelease(offenderId: String, sentenceId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri(constructUri(offenderId, sentenceId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUri(offenderId: String, sentenceId: String) =
    "/offender/$offenderId/sentence/$sentenceId/release"
}
