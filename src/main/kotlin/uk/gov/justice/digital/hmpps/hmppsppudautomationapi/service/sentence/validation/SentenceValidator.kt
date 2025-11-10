package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.sentence.validation

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.DETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.INDETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException

// Limit at which PPUD truncates
private const val MAX_SENTENCING_COURT_LENGTH = 50

@Service
internal class SentenceValidator {

  fun validateSentenceCreationRequest(request: CreateOrUpdateSentenceRequest) {
    val custodyType = validateCustodyType(request.custodyType, "create")

    when (custodyType.custodyGroup) {
      DETERMINATE -> {
        validateDeterminateSentenceRequest(request, "create")
      }

      INDETERMINATE -> {
        throw ValidationException("Request to create an indeterminate sentence unsupported (only determinate sentence creation is supported)")
      }
    }
  }

  fun validateSentenceUpdateRequest(request: CreateOrUpdateSentenceRequest) {
    val custodyType = validateCustodyType(request.custodyType, "update")

    when (custodyType.custodyGroup) {
      DETERMINATE -> {
        validateDeterminateSentenceRequest(request, "update")
      }

      INDETERMINATE -> {
        // do nothing - the only requirement for Indeterminate sentences is a
        // sentence date, which is non-nullable in a CreateOrUpdateSentenceRequest
      }
    }
  }

  private fun validateCustodyType(custodyTypeName: String, requestedAction: String): SupportedCustodyType {
    if (custodyTypeName.isBlank()) {
      throw ValidationException("Request to $requestedAction a sentence was missing a Custody Type value")
    }

    val custodyType = try {
      SupportedCustodyType.forFullName(custodyTypeName)
    } catch (e: NoSuchElementException) {
      throw UnsupportedCustodyTypeException("Request to $requestedAction a sentence had an invalid Custody Type value: $custodyTypeName")
    }

    return custodyType
  }

  private fun validateDeterminateSentenceRequest(request: CreateOrUpdateSentenceRequest, requestedAction: String) {
    validateMappaLevel(request, requestedAction)
    validateSentencedUnder(request, requestedAction)
    validateSentencingCourt(request, requestedAction)
    // no need to validate the sentence date, as it's non-nullable in CreateOrUpdateSentenceRequest
  }

  private fun validateMappaLevel(request: CreateOrUpdateSentenceRequest, requestedAction: String) {
    // PPUD requires determinate sentences to have a MAPPA Level set
    if (request.mappaLevel.isNullOrBlank()) {
      throw ValidationException("Request to $requestedAction a determinate sentence was missing a MAPPA Level")
    }
  }

  private fun validateSentencedUnder(request: CreateOrUpdateSentenceRequest, requestedAction: String) {
    // This field should have the same value as the sentencedUnder value for a
    // release. Since the latter is required by PPUD, we also require it here
    if (request.sentencedUnder.isNullOrBlank()) {
      throw ValidationException("Request to $requestedAction a determinate sentence was missing a Sentenced Under value")
    }
  }

  private fun validateSentencingCourt(request: CreateOrUpdateSentenceRequest, requestedAction: String) {
    // Otherwise, PPUD truncates the value set in the Sentencing Court field without warning
    if (request.sentencingCourt.length > MAX_SENTENCING_COURT_LENGTH) {
      throw ValidationException("Request to $requestedAction a determinate sentence had a Sentencing Court exceeding $MAX_SENTENCING_COURT_LENGTH characters")
    }
  }
}
