package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_OFFENDER_ID_WITH_EMPTY_RELEASE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPostcode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Consumer

@ExtendWith(OffenderGetTest.OffenderUpdateDataTidyExtension::class)
class OffenderGetTest : IntegrationTestBase() {

  internal class OffenderUpdateDataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      println("TestRunId for this run: $testRunId")
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
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
    retrieveOffender(ppudKnownExistingOffender.id)
      .jsonPath("offender.sentences[0].custodyType").isEqualTo("Determinate")
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo("2003-06-12")
      .jsonPath("offender.sentences[0].espCustodialPeriod.years").isEqualTo(1)
      .jsonPath("offender.sentences[0].espCustodialPeriod.months").isEqualTo(2)
      .jsonPath("offender.sentences[0].espExtendedPeriod.years").isEqualTo(7)
      .jsonPath("offender.sentences[0].espExtendedPeriod.months").isEqualTo(8)
      .jsonPath("offender.sentences[0].licenceExpiryDate").isEqualTo("2020-06-30")
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo("Level 2 – Local Inter-Agency Management")
      .jsonPath("offender.sentences[0].releaseDate").isEqualTo("2013-02-15")
      .jsonPath("offender.sentences[0].sentencedUnder").isEqualTo("CJA 1991")
      .jsonPath("offender.sentences[0].sentenceExpiryDate").isEqualTo("2014-01-01")
      .jsonPath("offender.sentences[0].sentenceLength.partYears").isEqualTo(4)
      .jsonPath("offender.sentences[0].sentenceLength.partMonths").isEqualTo(5)
      .jsonPath("offender.sentences[0].sentenceLength.partDays").isEqualTo(6)
      .jsonPath("offender.sentences[0].sentencingCourt").isEqualTo("Leeds")
  }

  @Test
  fun `given Offender with indeterminate sentence when get offender called then sentence is returned`() {
    retrieveOffender(ppudKnownExistingOffender.id)
      .jsonPath("offender.sentences[1].custodyType").isEqualTo("Indeterminate (life)")
      .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo("2010-09-01")
      .jsonPath("offender.sentences[1].espCustodialPeriod").isEmpty
      .jsonPath("offender.sentences[1].espExtendedPeriod").isEmpty
      .jsonPath("offender.sentences[1].licenceExpiryDate").isEmpty
      .jsonPath("offender.sentences[1].mappaLevel").isEqualTo("")
      .jsonPath("offender.sentences[1].releaseDate").isEmpty
      .jsonPath("offender.sentences[1].sentencedUnder").isEmpty
      .jsonPath("offender.sentences[1].sentenceExpiryDate").isEmpty
      .jsonPath("offender.sentences[1].sentenceLength").isEmpty
      .jsonPath("offender.sentences[1].sentencingCourt").isEqualTo("Sheffield")
  }

  @Test
  fun `given Offender with sentence when get offender called then offence is returned`() {
    retrieveOffender(ppudKnownExistingOffender.id)
      .jsonPath("offender.sentences[0].offence.indexOffence").isEqualTo("ATTEMPTED MURDER")
      .jsonPath("offender.sentences[0].offence.dateOfIndexOffence").isEqualTo("2001-02-12")
      .jsonPath("offender.sentences[1].offence.indexOffence").isEqualTo("Not Specified")
      .jsonPath("offender.sentences[1].offence.dateOfIndexOffence").isEmpty
  }

  @Test
  fun `given Offender with release when get offender called then release is returned`() {
    retrieveOffender(ppudKnownExistingOffender.id)
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isEqualTo("2013-02-02")
      .jsonPath("offender.sentences[0].releases[0].releaseType").isEqualTo("On Licence")
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isEqualTo("CJA 2008")
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isEqualTo("HMP Wakefield")
      .jsonPath("offender.sentences[0].releases[0].category").isEqualTo("Not Applicable")
      .jsonPath("offender.sentences[1].releases.size()").isEqualTo(0)
  }

  @Test
  fun `given Offender with release when get offender called then Post Release is returned`() {
    retrieveOffender(ppudKnownExistingOffender.id)
      .jsonPath("offender.sentences[0].releases[0].postRelease.assistantChiefOfficer.name").isEqualTo("Joe Bloggs")
      .jsonPath("offender.sentences[0].releases[0].postRelease.assistantChiefOfficer.faxEmail")
      .isEqualTo("Joe.Bloggs@example.com")
      .jsonPath("offender.sentences[0].releases[0].postRelease.licenceType").isEqualTo("Standard")
      .jsonPath("offender.sentences[0].releases[0].postRelease.offenderManager.name").isEqualTo("Jane Doe")
      .jsonPath("offender.sentences[0].releases[0].postRelease.offenderManager.faxEmail")
      .isEqualTo("Jane.Doe@example.com")
      .jsonPath("offender.sentences[0].releases[0].postRelease.offenderManager.telephone").isEqualTo("099 1234567")
      .jsonPath("offender.sentences[0].releases[0].postRelease.probationService").isEqualTo("Merseyside")
      .jsonPath("offender.sentences[0].releases[0].postRelease.spoc.name").isEqualTo("Merseyside Constabulary")
      .jsonPath("offender.sentences[0].releases[0].postRelease.spoc.faxEmail").isEqualTo("spoc@example.com")
  }

  @Test
  fun `given Offender with empty release when get offender called then release is not returned`() {
    retrieveOffender(PPUD_OFFENDER_ID_WITH_EMPTY_RELEASE)
      .jsonPath("offender.sentences[0].releases.size()").isEqualTo(0)
  }

  @Test
  fun `given Offender with empty release and includeEmptyReleases is set to true when get offender called then release is returned`() {
    retrieveOffender(PPUD_OFFENDER_ID_WITH_EMPTY_RELEASE, includeEmptyReleases = true)
      .jsonPath("offender.sentences[0].releases.size()").isEqualTo(1)
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
}
