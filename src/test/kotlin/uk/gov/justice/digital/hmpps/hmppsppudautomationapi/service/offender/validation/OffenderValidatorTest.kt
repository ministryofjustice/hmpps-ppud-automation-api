package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender.validation

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.randomDeterminateCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest

class OffenderValidatorTest {
  private val validator = OffenderValidator()

  /******************************
   * Creation Request Validation
   *****/

  @Test
  fun `validates correct offender with determinate sentence creation request with all relevant fields set`() {
    // given
    val creationRequest = generateCreateOffenderRequest(custodyType = randomDeterminateCustodyType())

    // when
    validator.validateOffenderCreationRequest(creationRequest)

    // then
    // No assertions - just checking no exceptions are thrown
  }

  @Test
  fun `invalidates offender creation request with blank custody type`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "")
    val expectedExceptionMessage = "Request to create an offender was missing a custodyType value"

    // when then
    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with invalid custody type`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "invalid")
    val expectedExceptionMessage = "Request to create an offender had an invalid custodyType value: invalid"

    assertThatThrownBy({ validator.validateOffenderCreationRequest(creationRequest) })
      .isInstanceOf(UnsupportedCustodyTypeException::class.java)
      .hasMessage(expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank ethnicity value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", ethnicity = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid ethnicity value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank firstNames value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", firstNames = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid firstNames value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank familyName value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", familyName = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid familyName value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank gender value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", gender = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid gender value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank indexOffence value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", indexOffence = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid indexOffence value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank mappaLevel value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", mappaLevel = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid mappaLevel value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  @Test
  fun `invalidates offender creation request with blank prisonNumber value`() {
    val creationRequest = generateCreateOffenderRequest(custodyType = "Determinate", prisonNumber = "")
    val expectedExceptionMessage = "Request to create an offender had an invalid prisonNumber value: "

    testInvalidCreationRequest(creationRequest, expectedExceptionMessage)
  }

  private fun testInvalidCreationRequest(
    creationRequest: CreateOffenderRequest,
    expectedExceptionMessage: String,
  ) {
    testInvalidRequest(expectedExceptionMessage) { validator.validateOffenderCreationRequest(creationRequest) }
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
