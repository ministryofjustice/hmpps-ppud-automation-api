package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.sentence.validation

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.randomDeterminateCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.randomIndeterminateCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.createOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomStringOfLength

class SentenceValidatorTest {

  private val validator = SentenceValidator()

  /////////////////////////////////
  // CREATION REQUEST VALIDATION //
  /////////////////////////////////

  @Test
  fun `validates correct determinate sentence creation request with all relevant fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType())

    // when
    validator.validateSentenceCreationRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `validates correct determinate sentence creation request with only required fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(
      custodyType = randomDeterminateCustodyType(),
      licenceExpiryDate = null,
      releaseDate = null,
      sentenceLength = null,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      sentenceExpiryDate = null,
      sentencingCourt = "",
    )

    // when
    validator.validateSentenceCreationRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `invalidates determinate sentence creation request with no MAPPA level`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), mappaLevel = null)
    val expectedExceptionMessage = "Request to create a determinate sentence was missing a MAPPA Level"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence creation request with blank MAPPA level`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), mappaLevel = "")
    val expectedExceptionMessage = "Request to create a determinate sentence was missing a MAPPA Level"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence creation request with no sentenced under`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), sentencedUnder = null)
    val expectedExceptionMessage = "Request to create a determinate sentence was missing a Sentenced Under value"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence creation request with blank sentenced under`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), sentencedUnder = "")
    val expectedExceptionMessage = "Request to create a determinate sentence was missing a Sentenced Under value"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence creation request with overlong (more than 50 characters) sentencing court`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(
        custodyType = randomDeterminateCustodyType(),
        sentencingCourt = randomStringOfLength(51),
      )
    val expectedExceptionMessage =
      "Request to create a determinate sentence had a Sentencing Court exceeding 50 characters"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates indeterminate sentence creation request`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomIndeterminateCustodyType())
    val expectedExceptionMessage =
      "Request to create an indeterminate sentence unsupported (only determinate sentence creation is supported)"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates sentence creation request with invalid custody type`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest()
    val expectedExceptionMessage =
      "Request to create a sentence had an invalid Custody Type value: ${creationRequest.custodyType}"

    // when then
    assertThatThrownBy { validator.validateSentenceCreationRequest(creationRequest) }
      .isInstanceOf(UnsupportedCustodyTypeException::class.java)
      .hasMessage(expectedExceptionMessage)
  }

  @Test
  fun `invalidates sentence creation request with blank custody type`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = "")
    val expectedExceptionMessage = "Request to create a sentence was missing a Custody Type value"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  ///////////////////////////////
  // UPDATE REQUEST VALIDATION //
  ///////////////////////////////

  @Test
  fun `validates correct determinate sentence update request with all relevant fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType())

    // when
    validator.validateSentenceUpdateRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `validates correct determinate sentence update request with only required fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(
      custodyType = randomDeterminateCustodyType(),
      licenceExpiryDate = null,
      releaseDate = null,
      sentenceLength = null,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      sentenceExpiryDate = null,
      sentencingCourt = "",
    )

    // when
    validator.validateSentenceUpdateRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `invalidates determinate sentence update request with no MAPPA level`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), mappaLevel = null)
    val expectedExceptionMessage = "Request to update a determinate sentence was missing a MAPPA Level"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence update request with blank MAPPA level`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), mappaLevel = "")
    val expectedExceptionMessage = "Request to update a determinate sentence was missing a MAPPA Level"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence update request with no sentenced under`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), sentencedUnder = null)
    val expectedExceptionMessage = "Request to update a determinate sentence was missing a Sentenced Under value"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence update request with blank sentenced under`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = randomDeterminateCustodyType(), sentencedUnder = "")
    val expectedExceptionMessage = "Request to update a determinate sentence was missing a Sentenced Under value"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates determinate sentence update request with overlong (more than 50 characters) sentencing court`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(
        custodyType = randomDeterminateCustodyType(),
        sentencingCourt = randomStringOfLength(51),
      )
    val expectedExceptionMessage =
      "Request to update a determinate sentence had a Sentencing Court exceeding 50 characters"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `validates correct indeterminate sentence update request with all relevant fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(custodyType = randomIndeterminateCustodyType())

    // when
    validator.validateSentenceUpdateRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `validates correct indeterminate sentence update request with only required fields set`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest(
      custodyType = randomIndeterminateCustodyType(),
      licenceExpiryDate = null,
      mappaLevel = null,
      releaseDate = null,
      sentenceLength = null,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      sentenceExpiryDate = null,
      sentencingCourt = "",
      sentencedUnder = null,
    )

    // when
    validator.validateSentenceUpdateRequest(creationRequest)

    // then
    // nothing to assert - we just check no exceptions are thrown
  }

  @Test
  fun `invalidates sentence update request with invalid custody type`() {
    // given
    val creationRequest = createOrUpdateSentenceRequest()
    val expectedExceptionMessage =
      "Request to update a sentence had an invalid Custody Type value: ${creationRequest.custodyType}"

    // when then
    assertThatThrownBy { validator.validateSentenceUpdateRequest(creationRequest) }
      .isInstanceOf(UnsupportedCustodyTypeException::class.java)
      .hasMessage(expectedExceptionMessage)
  }

  @Test
  fun `invalidates sentence update request with blank custody type`() {
    // given
    val creationRequest =
      createOrUpdateSentenceRequest(custodyType = "")
    val expectedExceptionMessage = "Request to update a sentence was missing a Custody Type value"

    // when then
    testInvalidUpdateRequest(creationRequest, expectedExceptionMessage)
  }

  private fun testInvalidCreationRequest(
    creationRequest: CreateOrUpdateSentenceRequest,
    expectedExceptionMessage: String,
  ) {
    testInvalidRequest(expectedExceptionMessage) { validator.validateSentenceCreationRequest(creationRequest) }
  }

  private fun testInvalidUpdateRequest(
    updateRequest: CreateOrUpdateSentenceRequest,
    expectedExceptionMessage: String,
  ) {
    testInvalidRequest(expectedExceptionMessage) { validator.validateSentenceUpdateRequest(updateRequest) }
  }

  private fun testInvalidRequest(
    expectedExceptionMessage: String,
    validationCall: () -> Unit,
  ) {

    // when then
    assertThatThrownBy(validationCall)
      .isInstanceOf(ValidationException::class.java)
      .hasMessage(expectedExceptionMessage)
  }

}