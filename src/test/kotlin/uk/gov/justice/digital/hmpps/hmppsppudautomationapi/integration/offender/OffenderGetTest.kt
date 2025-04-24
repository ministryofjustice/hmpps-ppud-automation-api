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
      retrieveOffender(id)
        .jsonPath("offender.sentences[0].custodyType").isEqualTo(determinateSentence.custodyType)
        .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo(determinateSentence.sentenceDate)
        .jsonPath("offender.sentences[0].espCustodialPeriod.years").isEqualTo(determinateSentence.espCustodialPeriod.years)
        .jsonPath("offender.sentences[0].espCustodialPeriod.months").isEqualTo(determinateSentence.espCustodialPeriod.months)
        .jsonPath("offender.sentences[0].espExtendedPeriod.years").isEqualTo(determinateSentence.espExtendedPeriod.years)
        .jsonPath("offender.sentences[0].espExtendedPeriod.months").isEqualTo(determinateSentence.espExtendedPeriod.months)
        .jsonPath("offender.sentences[0].licenceExpiryDate").isEqualTo(determinateSentence.licenseExpiryDate)
        .jsonPath("offender.sentences[0].mappaLevel").isEqualTo(determinateSentence.mappaLevel)
        .jsonPath("offender.sentences[0].releaseDate").isEqualTo(determinateSentence.releaseDate)
        .jsonPath("offender.sentences[0].sentencedUnder").isEqualTo(determinateSentence.sentencedUnder)
        .jsonPath("offender.sentences[0].sentenceExpiryDate").isEqualTo(determinateSentence.expiryDate)
        .jsonPath("offender.sentences[0].sentenceLength.partYears").isEqualTo(determinateSentence.sentenceLength.partYears)
        .jsonPath("offender.sentences[0].sentenceLength.partMonths").isEqualTo(determinateSentence.sentenceLength.partMonths)
        .jsonPath("offender.sentences[0].sentenceLength.partDays").isEqualTo(determinateSentence.sentenceLength.partDays)
        .jsonPath("offender.sentences[0].sentencingCourt").isEqualTo(determinateSentence.sentencingCourt)
    }
  }

  @Test
  fun `given Offender with indeterminate sentence when get offender called then sentence is returned`() {
    with(ppudKnownExistingOffender) {
      retrieveOffender(id)
        .jsonPath("offender.sentences[1].custodyType").isEqualTo(indeterminateSentence.custodyType)
        .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo(indeterminateSentence.sentenceDate)
        .jsonPath("offender.sentences[1].espCustodialPeriod").isEmpty
        .jsonPath("offender.sentences[1].espExtendedPeriod").isEmpty
        .jsonPath("offender.sentences[1].licenceExpiryDate").isEmpty
        .jsonPath("offender.sentences[1].mappaLevel").isEmpty
        .jsonPath("offender.sentences[1].releaseDate").isEqualTo(indeterminateSentence.releaseDate)
        .jsonPath("offender.sentences[1].sentencedUnder").isEmpty
        .jsonPath("offender.sentences[1].sentenceExpiryDate").isEqualTo(indeterminateSentence.expiryDate)
        .jsonPath("offender.sentences[1].sentenceLength.partYears").isEqualTo(indeterminateSentence.sentenceLength.partYears)
        .jsonPath("offender.sentences[1].sentenceLength.partMonths").isEqualTo(indeterminateSentence.sentenceLength.partMonths)
        .jsonPath("offender.sentences[1].sentenceLength.partDays").isEqualTo(indeterminateSentence.sentenceLength.partDays)
        .jsonPath("offender.sentences[1].sentencingCourt").isEqualTo(indeterminateSentence.sentencingCourt)
    }
  }

  @Test
  fun `given Offender with sentence when get offender called then offence is returned`() {
    with(ppudKnownExistingOffender) {
      retrieveOffender(id)
        .jsonPath("offender.sentences[0].offence.indexOffence").isEqualTo(determinateSentence.offence.indexOffence)
        .jsonPath("offender.sentences[0].offence.dateOfIndexOffence").isEqualTo(determinateSentence.offence.dateOfIndexOffence.orEmpty())
        .jsonPath("offender.sentences[1].offence.indexOffence").isEqualTo(indeterminateSentence.offence.indexOffence)
        .jsonPath("offender.sentences[1].offence.dateOfIndexOffence").isEmpty
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
