package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender.validation

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException

@Service
internal class OffenderValidator {

  fun validateOffenderCreationRequest(request: CreateOffenderRequest) {
    val custodyType = validateCustodyType(request.custodyType, "create")

    when (custodyType.custodyGroup) {
      CustodyGroup.DETERMINATE -> {
        validateDeterminateOffenderCreateRequest(request)
      }

      CustodyGroup.INDETERMINATE -> {
        throw ValidationException("Request to create a new indeterminate offender PPUD record is unsupported")
      }
    }
  }

  private fun validateCustodyType(custodyTypeName: String, requestedAction: String): SupportedCustodyType {
    if (custodyTypeName.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender was missing a custodyType value")
    }

    val custodyType = try {
      SupportedCustodyType.forFullName(custodyTypeName)
    } catch (e: NoSuchElementException) {
      throw UnsupportedCustodyTypeException("Request to $requestedAction an offender had an invalid custodyType value: $custodyTypeName")
    }

    return custodyType
  }

  private fun validateDeterminateOffenderCreateRequest(request: CreateOffenderRequest) {
    validateEthinicity(request, "create")
    validateFirstNames(request, "create")
    validateFamilyName(request, "create")
    validateGender(request, "create")
    validateIndexOffences(request, "create")
    validateMappaLevel(request, "create")
    validatePrisonNumber(request, "create")
  }

  private fun validateEthinicity(request: CreateOffenderRequest, requestedAction: String) {
    if (request.ethnicity.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank ethnicity value")
    }
  }

  private fun validateFirstNames(request: CreateOffenderRequest, requestedAction: String) {
    if (request.firstNames.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank firstNames value")
    }
  }

  private fun validateFamilyName(request: CreateOffenderRequest, requestedAction: String) {
    if (request.familyName.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank familyName value")
    }
  }

  private fun validateGender(request: CreateOffenderRequest, requestedAction: String) {
    if (request.gender.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank gender value")
    }
  }

  private fun validateIndexOffences(request: CreateOffenderRequest, requestedAction: String) {
    if (request.indexOffence.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank indexOffence value")
    }
  }

  private fun validateMappaLevel(request: CreateOffenderRequest, requestedAction: String) {
    if (request.mappaLevel.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank mappaLevel value")
    }
  }

  private fun validatePrisonNumber(request: CreateOffenderRequest, requestedAction: String) {
    if (request.prisonNumber.isBlank()) {
      throw ValidationException("Request to $requestedAction an offender had a blank prisonNumber value")
    }
  }
}
