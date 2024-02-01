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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_INDEX_OFFENCE_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderOffenceUpdateTest.DataTidyExtension::class)
class OffenderOffenceUpdateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("indexOffence", updateOffenceRequestBody(indexOffence = "")),
      )
    }

    private fun updateOffenceRequestBody(
      indexOffence: String = PPUD_VALID_INDEX_OFFENCE,
      dateOfIndexOffence: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
    ): String {
      return """ 
        {
          "indexOffence":"$indexOffence",
          "dateOfIndexOffence":"$dateOfIndexOffence" 
        }
      """.trimIndent()
    }
  }

  internal class DataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing request body when update offence called then bad request is returned`() {
    webTestClient.put()
      .uri(constructUpdateOffenceUri(randomPpudId(), randomPpudId()))
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when post release called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    putOffence(offenderId = randomPpudId(), sentenceId = randomPpudId(), requestBody = data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { Assertions.assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing optional fields in request body when update offence called then 200 OK is returned`() {
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val requestBodyWithOnlyMandatoryFields = """
      {
        "indexOffence":"$PPUD_VALID_INDEX_OFFENCE"
      }
    """.trimIndent()

    putOffence(offenderId, sentenceId, requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isOk
  }

  @Test
  fun `given missing token when update offence called then unauthorized is returned`() {
    val uri = constructUpdateOffenceUri(randomPpudId(), randomPpudId())
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, uri)
  }

  @Test
  fun `given token without recall role when update offence called then forbidden is returned`() {
    val requestBody = updateOffenceRequestBody()
    val uri = constructUpdateOffenceUri(randomPpudId(), randomPpudId())
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(uri, requestBody, HttpMethod.PUT)
  }

  @Test
  fun `given valid values in request body when update offence called then offence is updated using supplied values`() {
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val indexOffence = PPUD_VALID_INDEX_OFFENCE_2
    val dateOfIndexOffence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val requestBody = updateOffenceRequestBody(
      indexOffence = indexOffence,
      dateOfIndexOffence = dateOfIndexOffence,
    )

    testPutOffence(offenderId, sentenceId, requestBody)

    val retrieved = retrieveOffender(offenderId)
    retrieved
      .jsonPath("offender.sentences[0].id").isEqualTo(sentenceId)
      .jsonPath("offender.sentences[0].offence.indexOffence").isEqualTo(indexOffence)
      .jsonPath("offender.sentences[0].offence.dateOfIndexOffence").isEqualTo(dateOfIndexOffence)
  }

  private fun testPutOffence(offenderId: String, sentenceId: String, requestBody: String) {
    putOffence(offenderId, sentenceId, requestBody)
      .expectStatus()
      .isOk
      .expectBody()
      .isEmpty
  }

  private fun putOffence(offenderId: String, sentenceId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri(constructUpdateOffenceUri(offenderId, sentenceId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun constructUpdateOffenceUri(offenderId: String, sentenceId: String) =
    "/offender/$offenderId/sentence/$sentenceId/offence"
}
