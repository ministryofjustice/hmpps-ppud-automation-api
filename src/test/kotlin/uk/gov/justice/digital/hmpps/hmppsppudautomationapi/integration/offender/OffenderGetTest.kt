package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_OFFENDER_ID_WITH_PAGED_ADDRESSES
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPostcode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer

class OffenderGetTest : IntegrationTestBase() {

  @AfterAll
  fun afterAll() {
    println("TestRunId for this run: $testRunId")
    deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
  }

  @Test
  fun `given missing token when get offender called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.GET, "/offender/${randomPpudId()}")
  }

  @Test
  fun `given token without recall role when get offender called then forbidden is returned`() {
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned("/offender/${randomPpudId()}")
  }

  @Test
  fun `given invalid offender ID when get offender called then 400 BAD REQUEST with invalid ID error is returned`() {
    retrieveOffenderWhenNotOk("A7747DZ")
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains("Offender ID is invalid") })
  }

  @Test
  fun `given Offender with determinate sentence when get offender called then sentence is returned`() {
    with(ppudKnownExistingOffender) {
      val pathToSentence = "offender.sentences[0]"
      retrieveOffender(id)
        .jsonPath("$pathToSentence.custodyType").isEqualTo(determinateSentence.custodyType)
        .jsonPath("$pathToSentence.dateOfSentence").isEqualTo(determinateSentence.sentenceDate)
        .jsonPath("$pathToSentence.espCustodialPeriod.years").isEqualTo(determinateSentence.espCustodialPeriod.years)
        .jsonPath("$pathToSentence.espCustodialPeriod.months").isEqualTo(determinateSentence.espCustodialPeriod.months)
        .jsonPath("$pathToSentence.espExtendedPeriod.years").isEqualTo(determinateSentence.espExtendedPeriod.years)
        .jsonPath("$pathToSentence.espExtendedPeriod.months").isEqualTo(determinateSentence.espExtendedPeriod.months)
        .jsonPath("$pathToSentence.licenceExpiryDate").isEqualTo(determinateSentence.licenseExpiryDate)
        .jsonPath("$pathToSentence.mappaLevel").isEqualTo(determinateSentence.mappaLevel)
        .jsonPath("$pathToSentence.releaseDate").isEqualTo(determinateSentence.releaseDate)
        .jsonPath("$pathToSentence.sentencedUnder").isEqualTo(determinateSentence.sentencedUnder)
        .jsonPath("$pathToSentence.sentenceExpiryDate").isEqualTo(determinateSentence.expiryDate)
        .jsonPath("$pathToSentence.sentenceLength.partYears").isEqualTo(determinateSentence.sentenceLength.partYears)
        .jsonPath("$pathToSentence.sentenceLength.partMonths").isEqualTo(determinateSentence.sentenceLength.partMonths)
        .jsonPath("$pathToSentence.sentenceLength.partDays").isEqualTo(determinateSentence.sentenceLength.partDays)
        .jsonPath("$pathToSentence.sentencingCourt").isEqualTo(determinateSentence.sentencingCourt)
    }
  }

  @Test
  fun `given Offender with indeterminate sentence when get offender called then sentence is returned`() {
    with(ppudKnownExistingOffender) {
      val pathToSentence = "offender.sentences[1]"
      val pathToSentenceLength = "$pathToSentence.sentenceLength"
      retrieveOffender(id)
        .jsonPath("$pathToSentence.custodyType").isEqualTo(indeterminateSentence.custodyType)
        .jsonPath("$pathToSentence.dateOfSentence").isEqualTo(indeterminateSentence.sentenceDate)
        .jsonPath("$pathToSentence.espCustodialPeriod").isEmpty
        .jsonPath("$pathToSentence.espExtendedPeriod").isEmpty
        .jsonPath("$pathToSentence.licenceExpiryDate").isEmpty
        .jsonPath("$pathToSentence.mappaLevel").isEmpty
        .jsonPath("$pathToSentence.releaseDate").isEqualTo(indeterminateSentence.releaseDate)
        .jsonPath("$pathToSentence.sentencedUnder").isEmpty
        .jsonPath("$pathToSentence.tariffExpiryDate").isEqualTo(indeterminateSentence.expiryDate)
        .jsonPath("$pathToSentenceLength.partYears").isEqualTo(indeterminateSentence.sentenceLength.partYears)
        .jsonPath("$pathToSentenceLength.partMonths").isEqualTo(indeterminateSentence.sentenceLength.partMonths)
        .jsonPath("$pathToSentenceLength.partDays").isEqualTo(indeterminateSentence.sentenceLength.partDays)
        .jsonPath("$pathToSentence.sentencingCourt").isEqualTo(indeterminateSentence.sentencingCourt)
    }
  }

  @Test
  fun `given Offender with sentence when get offender called then offence is returned`() {
    with(ppudKnownExistingOffender) {
      val pathToFirstOffence = "offender.sentences[0].offence"
      val pathToSecondOffence = "offender.sentences[1].offence"
      retrieveOffender(id)
        .jsonPath("$pathToFirstOffence.indexOffence").isEqualTo(determinateSentence.offence.indexOffence)
        .jsonPath("$pathToFirstOffence.dateOfIndexOffence").isEqualTo(determinateSentence.offence.dateOfIndexOffence)
        .jsonPath("$pathToFirstOffence.offenceComment").isEqualTo(determinateSentence.offence.offenceComment)
        .jsonPath("$pathToSecondOffence.indexOffence").isEqualTo(indeterminateSentence.offence.indexOffence)
        .jsonPath("$pathToSecondOffence.dateOfIndexOffence").isEmpty
        .jsonPath("$pathToSecondOffence.offenceComment").isEmpty
    }
  }

  @ParameterizedTest
  @CsvSource(
    "premises,line1,line2,postcode,phoneNumber",
    "premises,'','','',''",
    "'','','','',phone",
    "'','',line2,'',''",
  )
  fun `given offender with partially or fully populated address when get offender called then address is returned`(
    premises: String,
    line1: String,
    line2: String,
    postcode: String,
    phoneNumber: String,
  ) {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        address = addressRequestBody(premises, line1, line2, postcode, phoneNumber),
      ),
    )

    retrieveOffender(offenderId)
      .jsonPath("offender.id").isEqualTo(offenderId)
      .jsonPath("offender.address.premises").isEqualTo(premises)
      .jsonPath("offender.address.line1").isEqualTo(line1)
      .jsonPath("offender.address.line2").isEqualTo(line2)
      .jsonPath("offender.address.postcode").isEqualTo(postcode)
      .jsonPath("offender.address.phoneNumber").isEqualTo(phoneNumber)
  }

  @Test
  fun `given offender with no address when get offender called then empty address is returned`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        address = addressRequestBody(premises = "", line1 = "", line2 = "", postcode = "", phoneNumber = ""),
      ),
    )

    retrieveOffender(offenderId)
      .jsonPath("offender.id").isEqualTo(offenderId)
      .jsonPath("offender.address.premises").isEmpty
      .jsonPath("offender.address.line1").isEmpty
      .jsonPath("offender.address.line2").isEmpty
      .jsonPath("offender.address.postcode").isEmpty
      .jsonPath("offender.address.phoneNumber").isEmpty
  }

  @Test
  fun `given offender with more than one address when get offender called then last address is returned`() {
    val premises = randomString("premises")
    val line1 = randomString("line1")
    val line2 = randomString("line2")
    val postcode = randomPostcode()
    val phoneNumber = randomPhoneNumber()

    val offenderId = createTestOffenderInPpud()
    val requestBody = updateOffenderRequestBody(
      address = addressRequestBody(premises, line1, line2, postcode, phoneNumber),
    )
    putOffender(offenderId, requestBody)

    retrieveOffender(offenderId)
      .jsonPath("offender.id").isEqualTo(offenderId)
      .jsonPath("offender.address.premises").isEqualTo(premises)
      .jsonPath("offender.address.line1").isEqualTo(line1)
      .jsonPath("offender.address.line2").isEqualTo(line2)
      .jsonPath("offender.address.postcode").isEqualTo(postcode)
      .jsonPath("offender.address.phoneNumber").isEqualTo(phoneNumber)
  }

  @Test
  fun `given Offender with paged addresses when get offender called then latest address is returned`() {
    val offenderId = PPUD_OFFENDER_ID_WITH_PAGED_ADDRESSES
    retrieveOffender(offenderId)
      .jsonPath("offender.id").isEqualTo(offenderId)
      .jsonPath("offender.address.premises").isEqualTo("Queen Vic - Latest")
      .jsonPath("offender.address.line1").isEqualTo("46 Albert Sq - Latest")
      .jsonPath("offender.address.line2").isEqualTo("Walford - Latest")
      .jsonPath("offender.address.postcode").isEqualTo("E20 6PQ L")
      .jsonPath("offender.address.phoneNumber").isEqualTo("Latest Entry")
  }
}
