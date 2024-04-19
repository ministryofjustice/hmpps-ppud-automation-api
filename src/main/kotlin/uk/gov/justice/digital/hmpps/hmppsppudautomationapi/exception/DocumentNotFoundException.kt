package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception

import java.util.UUID

class DocumentNotFoundException(documentId: UUID) : RuntimeException("Document with ID '$documentId' was not found.")
