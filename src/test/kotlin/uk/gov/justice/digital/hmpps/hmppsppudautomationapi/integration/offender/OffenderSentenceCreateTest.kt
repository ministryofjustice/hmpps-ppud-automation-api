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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderSentenceCreateTest.DataTidyExtension::class)
class OffenderSentenceCreateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("custodyType", createSentenceRequestBody(custodyType = "")),
      )
    }

    fun createSentenceRequestBody(
      custodyType: String = PPUD_VALID_CUSTODY_TYPE,
    ): String {
      return "{" +
        "\"custodyType\":\"$custodyType\" " +
        "}"
    }
  }

  internal class DataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing request body when create sentence called then bad request is returned`() {
    webTestClient.post()
      .uri(constructCreateSentenceUri(randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when create sentence called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val offenderId = randomPpudId()
    val errorFragment = data.errorFragment ?: data.propertyName
    postSentence(offenderId, data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given custody type is not determinate in request body when create sentence called then bad request is returned`() {
    // This is a temporary restriction until we handle indeterminate recalls
    val requestBody = createSentenceRequestBody(custodyType = randomString("custodyType"))
    postSentence(randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains("custodyType") })
  }

  @Test
  fun `given missing token when create sentence called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, constructCreateSentenceUri(randomPpudId()))
  }

  @Test
  fun `given token without recall role when create sentence called then forbidden is returned`() {
    val requestBody = createSentenceRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(constructCreateSentenceUri(randomPpudId()), requestBody)
  }

  private fun postSentence(offenderId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri(constructCreateSentenceUri(offenderId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructCreateSentenceUri(offenderId: String) =
    "/offender/$offenderId/sentence"
}
