package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

interface ReferenceService {
  suspend fun retrieveEstablishments(): List<String>
  suspend fun retrieveEthnicities(): List<String>
}
