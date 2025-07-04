package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.DocumentNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.InvalidOffenderIdException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ReleaseNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.SentenceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException

@RestControllerAdvice
class HmppsPpudAutomationApiExceptionHandler {
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
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
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
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
  fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
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
  fun handleInvalidOffenderIdException(e: InvalidOffenderIdException): ResponseEntity<ErrorResponse> {
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

  @ExceptionHandler(ReleaseNotFoundException::class)
  fun handleReleaseNotFoundException(e: ReleaseNotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Release not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Release was not found",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(SentenceNotFoundException::class)
  fun handleSentenceNotFoundException(e: SentenceNotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Sentence not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Sentence was not found",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(DocumentNotFoundException::class)
  fun handleDocumentNotFoundException(e: DocumentNotFoundException): ResponseEntity<ErrorResponse> {
    log.info("Document not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Document was not found",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(UnsupportedCustodyTypeException::class)
  fun handleUnsupportedCustodyTypeException(e: UnsupportedCustodyTypeException): ResponseEntity<ErrorResponse> {
    log.info("Unsupported custody type exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Unsupported custody type found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(WebClientResponseException.InternalServerError::class)
  fun handleDownstreamDependencyErrorException(e: WebClientResponseException.InternalServerError): ResponseEntity<ErrorResponse> {
    log.info("Downstream dependency error exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_GATEWAY)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_GATEWAY,
          userMessage = "A system on which we depend has failed: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ClientTimeoutException::class)
  fun handleClientTimeoutException(e: ClientTimeoutException): ResponseEntity<ErrorResponse> {
    log.info("Client timeout exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.GATEWAY_TIMEOUT)
      .body(
        ErrorResponse(
          status = HttpStatus.GATEWAY_TIMEOUT,
          userMessage = "Client timeout: ${e.message}",
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
