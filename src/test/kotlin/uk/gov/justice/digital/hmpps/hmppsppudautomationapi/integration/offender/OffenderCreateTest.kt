package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudImmigrationStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudOffenderWithRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudPrisonerCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidEthnicity
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidGender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidIndexOffence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudValidMappaLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudYoungOffenderNo
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudYoungOffenderYes
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPncNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream

class OffenderCreateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("custodyType", createOffenderRequestBody(custodyType = "")),
        MandatoryFieldTestData("dateOfBirth", createOffenderRequestBody(dateOfBirth = "")),
        MandatoryFieldTestData("dateOfSentence", createOffenderRequestBody(dateOfSentence = "")),
        MandatoryFieldTestData("ethnicity", createOffenderRequestBody(ethnicity = "")),
        MandatoryFieldTestData("firstNames", createOffenderRequestBody(firstNames = "")),
        MandatoryFieldTestData("familyName", createOffenderRequestBody(familyName = "")),
        MandatoryFieldTestData("gender", createOffenderRequestBody(gender = "")),
        MandatoryFieldTestData("indexOffence", createOffenderRequestBody(indexOffence = "")),
        MandatoryFieldTestData("mappaLevel", createOffenderRequestBody(mappaLevel = "")),
        MandatoryFieldTestData("prisonNumber", createOffenderRequestBody(prisonNumber = "")),
      )
    }

    private fun createOffenderRequestBody(
      croNumber: String = randomCroNumber(),
      custodyType: String = ppudValidCustodyType,
      dateOfBirth: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      dateOfSentence: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      ethnicity: String = ppudValidEthnicity,
      firstNames: String = randomString("firstNames"),
      familyName: String = randomString("familyName"),
      gender: String = ppudValidGender,
      indexOffence: String = ppudValidIndexOffence,
      mappaLevel: String = ppudValidMappaLevel,
      nomsId: String = randomNomsId(),
      pncNumber: String = randomPncNumber(),
      prisonNumber: String = randomPrisonNumber(),
    ): String {
      return "{" +
        "\"croNumber\":\"${croNumber}\", " +
        "\"custodyType\":\"${custodyType}\", " +
        "\"dateOfBirth\":\"$dateOfBirth\", " +
        "\"dateOfSentence\":\"$dateOfSentence\", " +
        "\"ethnicity\":\"$ethnicity\", " +
        "\"familyName\":\"$familyName\", " +
        "\"firstNames\":\"$firstNames\", " +
        "\"gender\":\"$gender\", " +
        "\"indexOffence\":\"$indexOffence\", " +
        "\"mappaLevel\":\"$mappaLevel\", " +
        "\"nomsId\":\"$nomsId\", " +
        "\"pncNumber\":\"$pncNumber\", " +
        "\"prisonNumber\":\"$prisonNumber\" " +
        "}"
    }
  }

  @Test
  fun `given missing request body when create offender called then bad request is returned`() {
    webTestClient.post()
      .uri("/offender")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when create offender called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    postOffender(data.requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given missing optional fields in request body when create offender called then 210 created is returned`() {
    val requestBodyWithOnlyMandatoryFields = "{" +
      "\"custodyType\":\"${ppudValidCustodyType}\", " +
      "\"dateOfBirth\":\"${randomDate()}\", " +
      "\"dateOfSentence\":\"${randomDate()}\", " +
      "\"ethnicity\":\"$ppudValidEthnicity\", " +
      "\"familyName\":\"${randomString("familyName")}\", " +
      "\"firstNames\":\"${randomString("firstNames")}\", " +
      "\"gender\":\"$ppudValidGender\", " +
      "\"indexOffence\":\"$ppudValidIndexOffence\", " +
      "\"mappaLevel\":\"$ppudValidMappaLevel\", " +
      "\"prisonNumber\":\"${randomPrisonNumber()}\" " +
      "}"

    postOffender(requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isCreated
  }

  @Test
  fun `given missing token when create offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/offender")
  }

  @Test
  fun `given token without recall role when create offender called then forbidden is returned`() {
    val requestBody = createOffenderRequestBody()
    givenTokenWithoutRecallRoleWhenPostingThenForbiddenReturned("/offender", requestBody)
  }

  @Test
  fun `given complete set of valid values in request body when create offender called then 201 created and offender details are returned`() {
    val requestBody = createOffenderRequestBody()
    postOffender(requestBody)
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("offender.id").isNotEmpty()
  }

  // TODO: Need to decide what to do with PNC Number
  // TODO: Need to verify Index Offence
  @Test
  fun `given valid values in request body when create offender called then offender is created using supplied values`() {
    val croNumber = randomCroNumber()
    val dateOfBirth = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val dateOfSentence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val familyName = randomString("familyName")
    val firstNames = randomString("firstNames")
    val nomsId = randomNomsId()
    val pncNumber = randomPncNumber()
    val prisonNumber = randomPrisonNumber()
    val requestBody = createOffenderRequestBody(
      croNumber = croNumber,
      custodyType = ppudValidCustodyType,
      dateOfBirth = dateOfBirth,
      dateOfSentence = dateOfSentence,
      ethnicity = ppudValidEthnicity,
      familyName = familyName,
      firstNames = firstNames,
      gender = ppudValidGender,
      indexOffence = ppudValidIndexOffence,
      mappaLevel = ppudValidMappaLevel,
      nomsId = nomsId,
      pncNumber = pncNumber,
      prisonNumber = prisonNumber,
    )

    val id = testPostOffender(requestBody)

    val retrieved = retrieveOffender(id)
    retrieved.jsonPath("offender.id").isEqualTo(id)
      .jsonPath("offender.croOtherNumber").isEqualTo(croNumber)
      .jsonPath("offender.dateOfBirth").isEqualTo(dateOfBirth)
      .jsonPath("offender.ethnicity").isEqualTo(ppudValidEthnicity)
      .jsonPath("offender.familyName").isEqualTo(familyName)
      .jsonPath("offender.firstNames").isEqualTo(firstNames)
      .jsonPath("offender.gender").isEqualTo(ppudValidGender)
      .jsonPath("offender.immigrationStatus").isEqualTo(ppudImmigrationStatus)
      .jsonPath("offender.nomsId").isEqualTo(nomsId)
      .jsonPath("offender.prisonerCategory").isEqualTo(ppudPrisonerCategory)
      .jsonPath("offender.prisonNumber").isEqualTo(prisonNumber)
      .jsonPath("offender.status").isEqualTo(ppudStatus)
      .jsonPath("offender.sentences.size()").isEqualTo(1)
      .jsonPath("offender.sentences[0].custodyType").isEqualTo(ppudValidCustodyType)
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo(dateOfSentence)
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo(ppudValidMappaLevel)
  }

  @ParameterizedTest
  @CsvSource(
    "18,$ppudYoungOffenderYes",
    "50,$ppudYoungOffenderNo",
  )
  fun `given offender 21 years or younger when create offender called then offender marked as young offender`(
    age: Long,
    expected: String,
  ) {
    val dateOfBirth = LocalDate.now().minusYears(age).format(DateTimeFormatter.ISO_LOCAL_DATE)
    val requestBody = createOffenderRequestBody(
      dateOfBirth = dateOfBirth,
    )

    val id = testPostOffender(requestBody)

    val retrieved = retrieveOffender(id)
    retrieved.jsonPath("offender.id").isEqualTo(id)
      .jsonPath("offender.youngOffender").isEqualTo(expected)
  }

  @Test
  fun `given duplicate offender in request body when create offender called then error returned`() {
    val requestBody = createOffenderRequestBody(
      dateOfBirth = ppudOffenderWithRelease.dateOfBirth,
      familyName = ppudOffenderWithRelease.familyName,
      firstNames = ppudOffenderWithRelease.firstNames,
      prisonNumber = ppudOffenderWithRelease.prisonNumber,
    )
    postOffender(requestBody)
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("userMessage")
      .isEqualTo("Unexpected error: Duplicate details found on PPUD for this offender.")
  }

  @Test
  fun `given duplicate identifier in request body when create offender called then error returned`() {
    val requestBody = createOffenderRequestBody(
      prisonNumber = ppudOffenderWithRelease.prisonNumber,
    )
    postOffender(requestBody)
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("userMessage")
      .isEqualTo(
        "Unexpected error: Offender creation failed." +
          " There is already an offender in the system with this file reference/Prison Number/Noms ID. Please check.",
      )
  }

  private fun testPostOffender(requestBody: String): String {
    val idExtractor = ValueConsumer<String>()
    postOffender(requestBody)
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("offender.id").value(idExtractor)
    val id = idExtractor.value
    assertNotNull(id, "ID returned from create offender request is null")
    assertTrue(id!!.isNotEmpty(), "ID returned from create offender request is empty")
    return id
  }

  private fun postOffender(requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/offender")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  private fun retrieveOffender(id: String): WebTestClient.BodyContentSpec {
    return webTestClient.get()
      .uri("/offender/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }
}
