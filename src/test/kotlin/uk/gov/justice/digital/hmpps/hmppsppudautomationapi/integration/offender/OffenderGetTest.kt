package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_OFFENDER_ID_WITH_NOT_SPECIFIED_RELEASE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudOffenderWithRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPostcode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

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
  fun `given Offender with determinate and indeterminate sentences when get offender called then both sentences are returned`() {
    retrieveOffender(ppudOffenderWithRelease.id)
      .jsonPath("offender.sentences[0].custodyType").isEqualTo("Determinate")
      .jsonPath("offender.sentences[0].dateOfSentence").isEqualTo("2003-06-12")
      .jsonPath("offender.sentences[0].mappaLevel").isEqualTo("Level 2 – Local Inter-Agency Management")
      .jsonPath("offender.sentences[1].custodyType").isEqualTo("Indeterminate (life)")
      .jsonPath("offender.sentences[1].dateOfSentence").isEqualTo("2010-09-01")
      .jsonPath("offender.sentences[1].mappaLevel").isEqualTo("")
  }

  @Test
  fun `given Offender with release when get offender called then release is returned`() {
    retrieveOffender(ppudOffenderWithRelease.id)
      .jsonPath("offender.sentences[0].releases[0].dateOfRelease").isEqualTo("2013-02-02")
      .jsonPath("offender.sentences[0].releases[0].releaseType").isEqualTo("On Licence")
      .jsonPath("offender.sentences[0].releases[0].releasedUnder").isEqualTo("CJA 2008")
      .jsonPath("offender.sentences[0].releases[0].releasedFrom").isEqualTo("HMP Wakefield")
      .jsonPath("offender.sentences[0].releases[0].category").isEqualTo("A")
      .jsonPath("offender.sentences[1].releases.size()").isEqualTo(0)
  }

  @Test
  fun `given Offender with Not Specified release when get offender called then release is not returned`() {
    retrieveOffender(PPUD_OFFENDER_ID_WITH_NOT_SPECIFIED_RELEASE)
      .jsonPath("offender.sentences[0].releases.size()").isEqualTo(0)
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
