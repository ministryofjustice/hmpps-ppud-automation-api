package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.InvalidOffenderIdException

@RestControllerAdvice
class HmppsPpudAutomationApiExceptionHandler {
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
  fun handleAccessDeniedException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          userMessage = "Access denied",
          developerMessage = "Access denied",
        ),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    log.info("MethodArgumentNotValid exception: {}", e.message)
    val userMessage = e.allErrors.joinToString(", ") { error ->
      val fieldName = (error as FieldError).field
      val errorMessage: String = error.defaultMessage ?: "Does not meet requirements"
      "$fieldName: $errorMessage"
    }

    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: $userMessage",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleHttpMessageNotReadableException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("HttpMessageNotReadable exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "HTTP Message failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(InvalidOffenderIdException::class)
  fun handleInvalidOffenderIdException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Invalid offender ID exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Offender ID is invalid",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
