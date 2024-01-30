package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderSentenceUpdateTest.DataTidyExtension::class)
class OffenderSentenceUpdateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("custodyType", createOrUpdateSentenceRequestBody(custodyType = "")),
      )
    }
  }

  internal class DataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing request body when update sentence called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUpdateSentenceUri(randomPpudId(), randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when update sentence called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val offenderId = randomPpudId()
    val sentenceId = randomPpudId()
    val errorFragment = data.errorFragment ?: data.propertyName
    putSentence(offenderId, sentenceId, data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given custody type is not determinate in request body when update sentence called then bad request is returned`() {
    // This is a temporary restriction until we handle indeterminate recalls
    val requestBody = createOrUpdateSentenceRequestBody(custodyType = randomString("custodyType"))
    putSentence(randomPpudId(), randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains("custodyType") })
  }

  @Test
  fun `given missing token when update sentence called then unauthorized is returned`() {
    val uri = constructUpdateSentenceUri(randomPpudId(), randomPpudId())
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, uri)
  }

  @Test
  fun `given token without recall role when update sentence called then forbidden is returned`() {
    val requestBody = createOrUpdateSentenceRequestBody()
    val uri = constructUpdateSentenceUri(randomPpudId(), randomPpudId())
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(uri, requestBody, HttpMethod.PUT)
  }

  private fun putSentence(offenderId: String, sentenceId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUpdateSentenceUri(offenderId, sentenceId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUpdateSentenceUri(offenderId: String, sentenceId: String) =
    "/offender/$offenderId/sentence/$sentenceId"
}
