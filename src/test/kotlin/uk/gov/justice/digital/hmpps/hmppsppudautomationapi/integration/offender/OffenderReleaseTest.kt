package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_UNSUPPORTED_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.util.function.Consumer
import java.util.stream.Stream

class OffenderReleaseTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> = Stream.of(
      MandatoryFieldTestData("dateOfRelease", releaseRequestBody(dateOfRelease = "")),
      MandatoryFieldTestData("releasedFrom", releaseRequestBody(releasedFrom = "")),
      MandatoryFieldTestData("releasedUnder", releaseRequestBody(releasedUnder = "")),
    )
  }

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
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
    postRelease(offenderId = ppudKnownExistingOffender.id, sentenceId = randomPpudId(), requestBody = requestBody)
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
  fun `given valid values in request body when post release called then status is Ok and offender id and sentence id are still returned`() {
    val testOffenderId = createTestOffenderInPpud()
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(testOffenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!
    val requestBody = releaseRequestBody()

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()

    // TODO MRD-2750 support checking the created/updated release
    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
  }

  @Disabled("Once creation of offenders with indeterminate sentences and releases for their sentences is supported, this test can be enabled.")
  @Test
  fun `updates a release for an indeterminate sentence`() {
    val testOffenderId =
      createTestOffenderInPpud(createOffenderRequestBody(custodyType = SupportedCustodyType.MANDATORY_MLP.fullName))
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(testOffenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!
    val requestBody = releaseRequestBody()

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()

    // I'm not sure this test is worth having: when updating a release, only two of its fields are updated, and they
    // are set based on application config values, not values passed in the request, so this second POST call to update
    // the release won't effectively make any changes, since those two fields are also set based on the same config
    // values in the release creation case. One way of testing would be to manually change those two values for an
    // existing release in the system, then adjust this test's values such that said release is updated and the values
    // set back to the original config-based values
    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()

    // TODO MRD-2750 support checking the created/updated release and post release
    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
  }

  @Disabled(
    "To run this test, you need to comment out the @Pattern line for the custodyType field in CreateOffenderRequest;" +
      " otherwise, the offender creation step below will fail",
  )
  @Test
  fun `given unsupported custody type in sentence, release update fails with expected exception`() {
    val testOffenderId =
      createTestOffenderInPpud(createOffenderRequestBody(custodyType = PPUD_UNSUPPORTED_CUSTODY_TYPE))
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(testOffenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!

    val requestBody = releaseRequestBody()

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isEqualTo(HttpStatus.BAD_REQUEST)
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Unsupported custody type found") })
  }

  private fun postRelease(offenderId: String, sentenceId: String, requestBody: String): WebTestClient.ResponseSpec = webTestClient.post()
    .uri(constructUri(offenderId, sentenceId))
    .headers { it.authToken() }
    .contentType(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(requestBody))
    .exchange()

  private fun constructUri(offenderId: String, sentenceId: String) = "/offender/$offenderId/sentence/$sentenceId/release"
}
