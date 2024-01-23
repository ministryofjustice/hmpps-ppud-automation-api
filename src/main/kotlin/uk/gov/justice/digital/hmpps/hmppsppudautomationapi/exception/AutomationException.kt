package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception

open class AutomationException : RuntimeException {

  constructor(message: String) : super(message)

  constructor(message: String, cause: Exception) : super(message, cause)
}
