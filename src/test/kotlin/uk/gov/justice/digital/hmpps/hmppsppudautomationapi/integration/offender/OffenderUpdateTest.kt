package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_IMMIGRATION_STATUS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_PRISONER_CATEGORY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_STATUS
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
  fun `given valid values in request body when update offender called then offender is amended using supplied values`() {
    val originalIsInCustody = Random.nextBoolean()
    val newIsInCustody = originalIsInCustody.not()
    val originalAdditionalAddressPremises = randomString("originalpremises")
    val testOffenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        additionalAddresses = addressRequestBody(originalAdditionalAddressPremises, "", "", "", ""),
        isInCustody = originalIsInCustody.toString(),
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
    )

    putOffender(testOffenderId, requestBody)

    val expectedComments =
      "Additional address:${System.lineSeparator()}" +
        "$additionalAddressPremises, $additionalAddressLine1, $additionalAddressLine2, $additionalAddressPostcode, $additionalAddressPhoneNumber${System.lineSeparator()}" +
        System.lineSeparator() +
        "Additional address:${System.lineSeparator()}" +
        originalAdditionalAddressPremises
    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
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
    val testOffenderId = createTestOffenderInPpud()
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
    val testOffenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfBirth = originalDateOfBirth,
      ),
    )
    val amendUuid = UUID.randomUUID()
    familyNameToDeleteUuids.add(amendUuid) // Do this so we clear up test data
    val requestBody = updateOffenderRequestBody(
      dateOfBirth = newDateOfBirth,
    )

    putOffender(testOffenderId, requestBody)

    val retrieved = retrieveOffender(testOffenderId)
    retrieved
      .jsonPath("offender.id").isEqualTo(testOffenderId)
      .jsonPath("offender.dateOfBirth").isEqualTo(newDateOfBirth)
      .jsonPath("offender.youngOffender").isEqualTo(expected)
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
}
