package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_IMMIGRATION_STATUS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_PRISONER_CATEGORY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_STATUS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ESTABLISHMENT
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ESTABLISHMENT_NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_YOUNG_OFFENDER_NO
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_YOUNG_OFFENDER_YES
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPostcode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.random.Random

class OffenderCreateTest : IntegrationTestBase() {

  companion object {

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> = Stream.of(
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

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
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
  fun `given custody type is not determinate in request body when create offender called then bad request is returned`() {
    // This is a temporary restriction until we handle indeterminate recalls
    val requestBody = createOffenderRequestBody(custodyType = randomString("custodyType"))
    postOffender(requestBody)
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("custodyType") })
  }

  @Test
  fun `given missing optional fields in request body when create offender called then 201 created is returned`() {
    val requestBodyWithOnlyMandatoryFields =
      """
        {
          "custodyType" : "$PPUD_VALID_CUSTODY_TYPE",
          "dateOfBirth" : "${randomDate()}",
          "dateOfSentence" : "${randomDate()}",
          "ethnicity" : "$PPUD_VALID_ETHNICITY",
          "familyName" : "$FAMILY_NAME_PREFIX-$testRunId",
          "firstNames" : "${randomString("firstNames")}",
          "gender" : "$PPUD_VALID_GENDER",
          "indexOffence" : "$PPUD_VALID_INDEX_OFFENCE",
          "mappaLevel" : "$PPUD_VALID_MAPPA_LEVEL",
          "prisonNumber" : "${randomPrisonNumber()}",
          "establishment" : "$PPUD_VALID_ESTABLISHMENT"
        }
      """.trimIndent()

    postOffender(requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isCreated
  }

  @Test
  fun `given null optional string fields in request body when create offender called then nulls are treated as empty strings`() {
    val requestBodyWithNullOptionalFields =
      """ 
      {
        "address" : {
          "premises" : null,
          "line1" : null,
          "line2" : null,
          "postcode" : null,
          "phoneNumber" : null
        },
        "custodyType" : "$PPUD_VALID_CUSTODY_TYPE",
        "croNumber" : null,
        "dateOfBirth" : "${randomDate()}",
        "dateOfSentence" : "${randomDate()}",
        "ethnicity" : "$PPUD_VALID_ETHNICITY",
        "familyName" : "$FAMILY_NAME_PREFIX-$testRunId",
        "firstNames" : "${randomString("firstNames")}",
        "gender" : "$PPUD_VALID_GENDER",
        "indexOffence" : "$PPUD_VALID_INDEX_OFFENCE",
        "mappaLevel" : "$PPUD_VALID_MAPPA_LEVEL",
        "nomsId" : null,
        "prisonNumber" : "${randomPrisonNumber()}",
        "establishment" : "$PPUD_VALID_ESTABLISHMENT"
      }
      """.trimIndent()

    val createdOffender = testPostOffender(requestBodyWithNullOptionalFields)

    val retrieved = retrieveOffender(createdOffender.id)
    retrieved.jsonPath("offender.id").isEqualTo(createdOffender.id)
      .jsonPath("offender.address.premises").isEqualTo("")
      .jsonPath("offender.address.line1").isEqualTo("")
      .jsonPath("offender.address.line2").isEqualTo("")
      .jsonPath("offender.address.postcode").isEqualTo("")
      .jsonPath("offender.address.phoneNumber").isEqualTo("")
      .jsonPath("offender.croOtherNumber").isEqualTo("")
      .jsonPath("offender.nomsId").isEqualTo("")
  }

  @Test
  fun `given missing token when create offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/offender")
  }

  @Test
  fun `given token without recall role when create offender called then forbidden is returned`() {
    val requestBody = createOffenderRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned("/offender", requestBody)
  }

  @Test
  fun `given valid values in request body when create offender called then offender is created using supplied values and IDs are returned`() {
    val addressPremises = randomString("premises")
    val addressLine1 = randomString("line1")
    val addressLine2 = randomString("line2")
    val addressPostcode = randomPostcode()
    val addressPhoneNumber = randomPhoneNumber()
    val additionalAddressPremises = randomString("additional premises")
    val additionalAddressLine1 = randomString("additional line1")
    val additionalAddressLine2 = randomString("additional line2")
    val additionalAddressPostcode = randomPostcode()
    val additionalAddressPhoneNumber = randomPhoneNumber()
    val croNumber = randomCroNumber()
    val dateOfBirth = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val dateOfSentence = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val familyName = "$FAMILY_NAME_PREFIX-$testRunId"
    val firstNames = randomString("firstNames")
    val isInCustody = Random.nextBoolean()
    val nomsId = randomNomsId()
    val prisonNumber = randomPrisonNumber()
    val establishment = if (isInCustody) PPUD_VALID_ESTABLISHMENT else PPUD_VALID_ESTABLISHMENT_NOT_APPLICABLE
    val requestBody = createOffenderRequestBody(
      address = addressRequestBody(addressPremises, addressLine1, addressLine2, addressPostcode, addressPhoneNumber),
      additionalAddresses = addressRequestBody(
        additionalAddressPremises,
        additionalAddressLine1,
        additionalAddressLine2,
        additionalAddressPostcode,
        additionalAddressPhoneNumber,
      ),
      croNumber = croNumber,
      custodyType = PPUD_VALID_CUSTODY_TYPE,
      dateOfBirth = dateOfBirth,
      dateOfSentence = dateOfSentence,
      ethnicity = PPUD_VALID_ETHNICITY,
      familyName = familyName,
      firstNames = firstNames,
      gender = PPUD_VALID_GENDER,
      indexOffence = PPUD_VALID_INDEX_OFFENCE,
      isInCustody = isInCustody.toString(),
      mappaLevel = PPUD_VALID_MAPPA_LEVEL,
      nomsId = nomsId,
      prisonNumber = prisonNumber,
      establishment = establishment,
    )

    val createdOffender = testPostOffender(requestBody)

    val expectedComments =
      "Additional address:${System.lineSeparator()}" +
        "$additionalAddressPremises, $additionalAddressLine1, $additionalAddressLine2, $additionalAddressPostcode, $additionalAddressPhoneNumber"
    val retrieved = retrieveOffender(createdOffender.id)
    retrieved.jsonPath("offender.id").isEqualTo(createdOffender.id)
      .jsonPath("offender.address.premises").isEqualTo(addressPremises)
      .jsonPath("offender.address.line1").isEqualTo(addressLine1)
      .jsonPath("offender.address.line2").isEqualTo(addressLine2)
      .jsonPath("offender.address.postcode").isEqualTo(addressPostcode)
      .jsonPath("offender.address.phoneNumber").isEqualTo(addressPhoneNumber)
      .jsonPath("offender.comments").isEqualTo(expectedComments)
      .jsonPath("offender.croOtherNumber").isEqualTo(croNumber)
      .jsonPath("offender.dateOfBirth").isEqualTo(dateOfBirth)
      .jsonPath("offender.ethnicity").isEqualTo(PPUD_VALID_ETHNICITY)
      .jsonPath("offender.familyName").isEqualTo(familyName)
      .jsonPath("offender.firstNames").isEqualTo(firstNames)
      .jsonPath("offender.gender").isEqualTo(PPUD_VALID_GENDER)
      .jsonPath("offender.immigrationStatus").isEqualTo(PPUD_IMMIGRATION_STATUS)
      .jsonPath("offender.isInCustody").isEqualTo(isInCustody.toString())
      .jsonPath("offender.nomsId").isEqualTo(nomsId)
      .jsonPath("offender.prisonerCategory").isEqualTo(PPUD_PRISONER_CATEGORY)
      .jsonPath("offender.prisonNumber").isEqualTo(prisonNumber)
      .jsonPath("offender.status").isEqualTo(PPUD_STATUS)
      .jsonPath("offender.sentences.size()").isEqualTo(1)
      .jsonPath("offender.sentences[0].id").isEqualTo(createdOffender.sentence.id)
      .jsonPath("offender.sentences[0].custodyType").isEqualTo(PPUD_VALID_CUSTODY_TYPE)
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo(dateOfSentence)
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo(PPUD_VALID_MAPPA_LEVEL)
      .jsonPath("offender.sentences[0].offence.indexOffence").isEqualTo(PPUD_VALID_INDEX_OFFENCE)
      .jsonPath("offender.establishment").isEqualTo(establishment)
  }

  @ParameterizedTest
  @CsvSource(
    "18,$PPUD_YOUNG_OFFENDER_YES",
    "50,$PPUD_YOUNG_OFFENDER_NO",
  )
  fun `given offender 21 years or younger when create offender called then offender marked as young offender`(
    age: Long,
    expected: String,
  ) {
    val dateOfBirth = LocalDate.now().minusYears(age).format(DateTimeFormatter.ISO_LOCAL_DATE)
    val requestBody = createOffenderRequestBody(
      dateOfBirth = dateOfBirth,
    )

    val createdOffender = testPostOffender(requestBody)

    val retrieved = retrieveOffender(createdOffender.id)
    retrieved.jsonPath("offender.id").isEqualTo(createdOffender.id)
      .jsonPath("offender.youngOffender").isEqualTo(expected)
  }

  @ParameterizedTest
  @ValueSource(strings = ["F ( Was M )", "F( Was M )"])
  fun `given gender F was M in request body when create offender called then offender is created`(gender: String) {
    // PPUD has a bug where the gender value is inconsistent. We need to handle that as best we can.
    // PPUD itself doesn't handle it, because if set with one value in create offender, it can't be displayed in view offender
    val requestBody = createOffenderRequestBody(
      gender = gender,
    )

    testPostOffender(requestBody)
  }

  @Test
  fun `given duplicate offender in request body when create offender called then error returned`() {
    val requestBody = createOffenderRequestBody(
      dateOfBirth = ppudKnownExistingOffender.dateOfBirth,
      familyName = ppudKnownExistingOffender.familyName,
      firstNames = ppudKnownExistingOffender.firstNames,
      prisonNumber = ppudKnownExistingOffender.prisonNumber,
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
      prisonNumber = ppudKnownExistingOffender.prisonNumber,
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

  private fun testPostOffender(requestBody: String): CreatedOffender {
    val offenderIdExtractor = ValueConsumer<String>()
    val sentenceIdExtractor = ValueConsumer<String>()
    postOffender(requestBody)
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("offender.id").isNotEmpty
      .jsonPath("offender.id").value(offenderIdExtractor)
      .jsonPath("offender.sentence.id").isNotEmpty
      .jsonPath("offender.sentence.id").value(sentenceIdExtractor)
    val offenderId = offenderIdExtractor.value!!
    val sentenceId = sentenceIdExtractor.value!!
    return CreatedOffender(offenderId, CreatedSentence(sentenceId))
  }

  private fun postOffender(requestBody: String): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/offender")
    .headers { it.authToken() }
    .contentType(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(requestBody))
    .exchange()
}
