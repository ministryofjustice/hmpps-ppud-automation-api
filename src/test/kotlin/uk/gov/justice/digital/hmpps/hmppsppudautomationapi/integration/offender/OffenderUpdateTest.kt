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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderUpdateTest.OffenderUpdateDataTidyExtension::class)
class OffenderUpdateTest : IntegrationTestBase() {

  companion object {

    val familyNameToDeleteUuids = mutableSetOf<UUID>()

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("dateOfBirth", createOffenderRequestBody(dateOfBirth = "")),
        MandatoryFieldTestData("ethnicity", updateOffenderRequestBody(ethnicity = "")),
        MandatoryFieldTestData("familyName", updateOffenderRequestBody(familyName = "")),
        MandatoryFieldTestData("firstNames", updateOffenderRequestBody(firstNames = "")),
        MandatoryFieldTestData("gender", updateOffenderRequestBody(gender = "")),
        MandatoryFieldTestData("prisonNumber", updateOffenderRequestBody(prisonNumber = "")),
      )
    }

    private fun updateOffenderRequestBody(
      dateOfBirth: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      ethnicity: String = PPUD_VALID_ETHNICITY,
      familyName: String = "${FAMILY_NAME_PREFIX}-$testRunId",
      firstNames: String = randomString("firstNames"),
      gender: String = PPUD_VALID_GENDER,
      prisonNumber: String = randomPrisonNumber(),
    ): String {
      return "{" +
        "\"dateOfBirth\":\"$dateOfBirth\", " +
        "\"ethnicity\":\"$ethnicity\", " +
        "\"familyName\":\"$familyName\", " +
        "\"firstNames\":\"$firstNames\", " +
        "\"gender\":\"$gender\", " +
        "\"prisonNumber\":\"$prisonNumber\" " +
        "}"
    }
  }

  internal class OffenderUpdateDataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
      familyNameToDeleteUuids.forEach {
        deleteTestOffenders(FAMILY_NAME_PREFIX, it)
      }
    }
  }

  @Test
  fun `given missing request body when update offender called then bad request is returned`() {
    webTestClient.put()
      .uri("/offender/${randomPpudId()}")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `given invalid offender ID when update offender called then bad request is returned`() {
    val requestBody = updateOffenderRequestBody()
    putOffender(randomPpudId(), requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Offender ID is invalid") })
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when update offender called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    putOffender(randomPpudId(), data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing optional fields in request body when create offender called then 200 ok is returned`() {
    val testOffenderId = createTestOffenderInPpud()
    val requestBodyWithOnlyMandatoryFields = "{" +
      "\"dateOfBirth\":\"${randomDate()}\", " +
      "\"ethnicity\":\"$PPUD_VALID_ETHNICITY\", " +
      "\"familyName\":\"${FAMILY_NAME_PREFIX}-${testRunId}\", " +
      "\"firstNames\":\"${randomString("firstNames")}\", " +
      "\"gender\":\"$PPUD_VALID_GENDER\", " +
      "\"prisonNumber\":\"${randomPrisonNumber()}\" " +
      "}"

    putOffender(testOffenderId, requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isOk
  }

  @Test
  fun `given missing token when update offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.PUT, "/offender/${randomPpudId()}")
  }

  @Test
  fun `given token without recall role when update offender called then forbidden is returned`() {
    val requestBody = updateOffenderRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
      "/offender/${randomPpudId()}",
      requestBody,
      HttpMethod.PUT,
    )
  }

  @Test
  fun `given complete set of valid values in request body when update offender called then 200 ok is returned`() {
    val testOffenderId = createTestOffenderInPpud()
    val requestBody = updateOffenderRequestBody()
    putOffender(testOffenderId, requestBody)
      .expectStatus()
      .isOk
  }

  @Test
  fun `given valid values in request body when update offender called then offender is amended using supplied values`() {
    val testOffenderId = createTestOffenderInPpud()
    val amendUuid = UUID.randomUUID()
    familyNameToDeleteUuids.add(amendUuid)
    val dateOfBirth = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val ethnicity = PPUD_VALID_ETHNICITY_2
    val familyName = "$FAMILY_NAME_PREFIX-$amendUuid"
    val firstNames = randomString("firstNames")
    val gender = PPUD_VALID_GENDER_2
    val prisonNumber = randomPrisonNumber()
    val requestBody = updateOffenderRequestBody(
      dateOfBirth = dateOfBirth,
      ethnicity = ethnicity,
      familyName = familyName,
      firstNames = firstNames,
      gender = gender,
      prisonNumber = prisonNumber,
    )

    putOffender(testOffenderId, requestBody)

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.dateOfBirth").isEqualTo(dateOfBirth)
      .jsonPath("offender.ethnicity").isEqualTo(ethnicity)
      .jsonPath("offender.familyName").isEqualTo(familyName)
      .jsonPath("offender.firstNames").isEqualTo(firstNames)
      .jsonPath("offender.gender").isEqualTo(gender)
      .jsonPath("offender.prisonNumber").isEqualTo(prisonNumber)
  }

  @Test
  fun `given capitalised names in request body when update offender called then offender is amended using supplied values`() {
    val testOffenderId = createTestOffenderInPpud()
    val familyName = "$FAMILY_NAME_PREFIX-$testRunId".uppercase()
    val firstNames = randomString("firstNames").uppercase()
    val requestBody = updateOffenderRequestBody(
      familyName = familyName,
      firstNames = firstNames,
    )

    putOffender(testOffenderId, requestBody)

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.familyName").isEqualTo(familyName)
      .jsonPath("offender.firstNames").isEqualTo(firstNames)
  }

  private fun putOffender(offenderId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/offender/$offenderId")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
}
