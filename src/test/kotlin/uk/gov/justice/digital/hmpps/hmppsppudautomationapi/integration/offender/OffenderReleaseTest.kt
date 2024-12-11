package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_PROBATION_SERVICE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer
import java.util.stream.Stream

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
    val assistantChiefOfficerName = randomString("acoName")
    val assistantChiefOfficerFaxEmail = randomString("acoFaxEmail")
    val offenderManagerName = randomString("omName")
    val offenderManagerFaxEmail = randomString("omFaxEmail")
    val offenderManagerTelephone = randomPhoneNumber()
    val probationService = PPUD_VALID_PROBATION_SERVICE
    val spocName = randomString("spocName")
    val spocFaxEmail = randomString("spocFaxEmail")
    val requestBody = releaseRequestBody(
      postRelease = postReleaseRequestBody(
        assistantChiefOfficerName = assistantChiefOfficerName,
        assistantChiefOfficerFaxEmail = assistantChiefOfficerFaxEmail,
        offenderManagerName = offenderManagerName,
        offenderManagerFaxEmail = offenderManagerFaxEmail,
        offenderManagerTelephone = offenderManagerTelephone,
        probationService = probationService,
        spocName = spocName,
        spocFaxEmail = spocFaxEmail,
      ),
    )

    postRelease(testOffenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
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
