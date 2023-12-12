package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

interface ReferenceService {
  suspend fun retrieveCustodyTypes(): List<String>

  suspend fun retrieveEstablishments(): List<String>

  suspend fun retrieveEthnicities(): List<String>

  suspend fun retrieveGenders(): List<String>

  suspend fun retrieveIndexOffences(): List<String>
}
