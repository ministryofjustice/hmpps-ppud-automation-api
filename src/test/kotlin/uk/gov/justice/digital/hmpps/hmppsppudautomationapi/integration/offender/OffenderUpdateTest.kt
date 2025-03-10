package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_IMMIGRATION_STATUS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_PRISONER_CATEGORY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_STATUS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ESTABLISHMENT
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ESTABLISHMENT_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ESTABLISHMENT_NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER_2
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_YOUNG_OFFENDER_NO
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_YOUNG_OFFENDER_YES
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPostcode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.random.Random

class OffenderUpdateTest : IntegrationTestBase() {

  private lateinit var testOffenderId: String

  companion object {

    val familyNameToDeleteUuids = mutableSetOf<UUID>()

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> = Stream.of(
      MandatoryFieldTestData("dateOfBirth", updateOffenderRequestBody(dateOfBirth = "")),
      MandatoryFieldTestData("ethnicity", updateOffenderRequestBody(ethnicity = "")),
      MandatoryFieldTestData("familyName", updateOffenderRequestBody(familyName = "")),
      MandatoryFieldTestData("firstNames", updateOffenderRequestBody(firstNames = "")),
      MandatoryFieldTestData("gender", updateOffenderRequestBody(gender = "")),
      MandatoryFieldTestData("prisonNumber", updateOffenderRequestBody(prisonNumber = "")),
    )
  }

  @BeforeAll
  fun beforeAll() {
    testOffenderId = createTestOffenderInPpud()
  }

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    familyNameToDeleteUuids.forEach {
      deleteTestOffenders(FAMILY_NAME_PREFIX, it)
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
  fun `given missing optional fields in request body when update offender called then 200 ok is returned`() {
    val requestBodyWithOnlyMandatoryFields =
      """
        {
          "dateOfBirth" : "${randomDate()}",
          "ethnicity" : "$PPUD_VALID_ETHNICITY",
          "familyName" : "$FAMILY_NAME_PREFIX-$testRunId",
          "firstNames" : "${randomString("firstNames")}",
          "gender" : "$PPUD_VALID_GENDER",
          "prisonNumber" : "${randomPrisonNumber()}",
          "establishment" : "$PPUD_VALID_ESTABLISHMENT"
        }
      """.trimIndent()

    putOffender(testOffenderId, requestBodyWithOnlyMandatoryFields)
      .expectStatus()
      .isOk
  }

  @Test
  fun `given null optional string fields in request body when update offender called then nulls are treated as empty string`() {
    val requestBodyWithNullOptionalFields =
      """
        {
          "croNumber" : null,
          "dateOfBirth" : "${randomDate()}",
          "ethnicity" : "$PPUD_VALID_ETHNICITY",
          "familyName" : "$FAMILY_NAME_PREFIX-$testRunId",
          "firstNames" : "${randomString("firstNames")}",
          "gender" : "$PPUD_VALID_GENDER",
          "nomsId" : null,
          "prisonNumber" : "${randomPrisonNumber()}",
          "establishment" : "$PPUD_VALID_ESTABLISHMENT"
        }
      """.trimIndent()

    putOffender(testOffenderId, requestBodyWithNullOptionalFields)
      .expectStatus()
      .isOk

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.croOtherNumber").isEqualTo("")
      .jsonPath("offender.nomsId").isEqualTo("")
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
  fun `given valid values in request body when update offender called then offender is amended using supplied values`() {
    val originalIsInCustody = Random.nextBoolean()
    val newIsInCustody = originalIsInCustody.not()
    val originalAdditionalAddressPremises = randomString("originalpremises")
    val localTestOffenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        additionalAddresses = addressRequestBody(originalAdditionalAddressPremises, "", "", "", ""),
        isInCustody = originalIsInCustody.toString(),
        establishment = if (originalIsInCustody) PPUD_VALID_ESTABLISHMENT else PPUD_VALID_ESTABLISHMENT_NOT_APPLICABLE,
      ),
    )
    val amendUuid = UUID.randomUUID()
    familyNameToDeleteUuids.add(amendUuid) // Do this so we clear up test data
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
    val ethnicity = PPUD_VALID_ETHNICITY_2
    val familyName = "$FAMILY_NAME_PREFIX-$amendUuid"
    val firstNames = randomString("firstNames")
    val gender = PPUD_VALID_GENDER_2
    val nomsId = randomNomsId()
    val prisonNumber = randomPrisonNumber()
    val establishment = if (newIsInCustody) PPUD_VALID_ESTABLISHMENT_2 else PPUD_VALID_ESTABLISHMENT_NOT_APPLICABLE
    val requestBody = updateOffenderRequestBody(
      address = addressRequestBody(addressPremises, addressLine1, addressLine2, addressPostcode, addressPhoneNumber),
      additionalAddresses = addressRequestBody(
        additionalAddressPremises,
        additionalAddressLine1,
        additionalAddressLine2,
        additionalAddressPostcode,
        additionalAddressPhoneNumber,
      ),
      croNumber = croNumber,
      dateOfBirth = dateOfBirth,
      ethnicity = ethnicity,
      familyName = familyName,
      firstNames = firstNames,
      gender = gender,
      isInCustody = newIsInCustody.toString(),
      nomsId = nomsId,
      prisonNumber = prisonNumber,
      establishment = establishment,
    )

    putOffender(localTestOffenderId, requestBody)

    val expectedComments =
      "Additional address:${System.lineSeparator()}" +
        "$additionalAddressPremises, $additionalAddressLine1, $additionalAddressLine2, $additionalAddressPostcode, $additionalAddressPhoneNumber${System.lineSeparator()}" +
        System.lineSeparator() +
        "Additional address:${System.lineSeparator()}" +
        originalAdditionalAddressPremises
    val retrieved = retrieveOffender(localTestOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(localTestOffenderId)
      .jsonPath("offender.address.premises").isEqualTo(addressPremises)
      .jsonPath("offender.address.line1").isEqualTo(addressLine1)
      .jsonPath("offender.address.line2").isEqualTo(addressLine2)
      .jsonPath("offender.address.postcode").isEqualTo(addressPostcode)
      .jsonPath("offender.address.phoneNumber").isEqualTo(addressPhoneNumber)
      .jsonPath("offender.comments").isEqualTo(expectedComments)
      .jsonPath("offender.croOtherNumber").isEqualTo(croNumber)
      .jsonPath("offender.dateOfBirth").isEqualTo(dateOfBirth)
      .jsonPath("offender.ethnicity").isEqualTo(ethnicity)
      .jsonPath("offender.familyName").isEqualTo(familyName)
      .jsonPath("offender.firstNames").isEqualTo(firstNames)
      .jsonPath("offender.gender").isEqualTo(gender)
      .jsonPath("offender.immigrationStatus").isEqualTo(PPUD_IMMIGRATION_STATUS)
      .jsonPath("offender.isInCustody").isEqualTo(newIsInCustody)
      .jsonPath("offender.nomsId").isEqualTo(nomsId)
      .jsonPath("offender.prisonerCategory").isEqualTo(PPUD_PRISONER_CATEGORY)
      .jsonPath("offender.prisonNumber").isEqualTo(prisonNumber)
      .jsonPath("offender.establishment").isEqualTo(establishment)
      .jsonPath("offender.status").isEqualTo(PPUD_STATUS)
  }

  @ParameterizedTest
  @CsvSource(
    "false,Recall UAL Checks",
    "true,Recall Reps Packs",
  )
  fun `given custody status when update offender called then caseworker is set accordingly`(
    isInCustody: Boolean,
    expectedCaseworker: String,
  ) {
    val requestBody = updateOffenderRequestBody(
      isInCustody = isInCustody.toString(),
    )

    putOffender(testOffenderId, requestBody)

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.caseworker").isEqualTo(expectedCaseworker)
      .jsonPath("offender.isInCustody").isEqualTo(isInCustody)
  }

  @ParameterizedTest
  @CsvSource(
    "17,18,$PPUD_YOUNG_OFFENDER_YES",
    "30,19,$PPUD_YOUNG_OFFENDER_YES",
    "17,50,$PPUD_YOUNG_OFFENDER_NO",
    "30,50,$PPUD_YOUNG_OFFENDER_NO",
  )
  fun `given new date of birth when update offender called then young offender is re-evaluated`(
    originalAge: Long,
    newAge: Long,
    expected: String,
  ) {
    val originalDateOfBirth = LocalDate.now().minusYears(originalAge).format(DateTimeFormatter.ISO_LOCAL_DATE)
    val newDateOfBirth = LocalDate.now().minusYears(newAge).format(DateTimeFormatter.ISO_LOCAL_DATE)
    val localTestOffenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfBirth = originalDateOfBirth,
      ),
    )
    val amendUuid = UUID.randomUUID()
    familyNameToDeleteUuids.add(amendUuid) // Do this so we clear up test data
    val requestBody = updateOffenderRequestBody(
      dateOfBirth = newDateOfBirth,
    )

    putOffender(localTestOffenderId, requestBody)

    val retrieved = retrieveOffender(localTestOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(localTestOffenderId)
      .jsonPath("offender.dateOfBirth").isEqualTo(newDateOfBirth)
      .jsonPath("offender.youngOffender").isEqualTo(expected)
  }

  @Test
  fun `given capitalised names in request body when update offender called then offender is amended using supplied values`() {
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

  @ParameterizedTest
  @ValueSource(strings = ["F ( Was M )", "F( Was M )"])
  fun `given gender F was M in request body when create offender called then offender is created`(gender: String) {
    // PPUD has a bug where the gender value is inconsistent. We need to handle that as best we can.
    // PPUD itself doesn't handle it, because if set with one value in create offender, it can't be displayed in view offender
    val requestBody = updateOffenderRequestBody(
      gender = gender,
    )

    putOffender(testOffenderId, requestBody)
      .expectStatus()
      .isOk
  }
}
