package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception

class ClientTimeoutException(clientName: String, errorType: String) : RuntimeException("$clientName: [$errorType]")
