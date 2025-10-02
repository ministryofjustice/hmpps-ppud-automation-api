package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

interface ReferenceService {
  fun clearCaches()

  suspend fun refreshCaches()

  @Deprecated("replaced by separate determinate and indeterminate methods")
  suspend fun retrieveCustodyTypes(): List<String>

  suspend fun retrieveEstablishments(): List<String>

  suspend fun retrieveEthnicities(): List<String>

  suspend fun retrieveGenders(): List<String>

  suspend fun retrieveIndexOffences(): List<String>

  suspend fun retrieveMappaLevels(): List<String>

  suspend fun retrievePoliceForces(): List<String>

  suspend fun retrieveProbationServices(): List<String>

  suspend fun retrieveReleasedUnders(): List<String>

  suspend fun retrieveDeterminateCustodyTypes(): List<String>

  suspend fun retrieveIndeterminateCustodyTypes(): List<String>

  suspend fun retrieveCourts(): List<String>

  fun quit()
}
