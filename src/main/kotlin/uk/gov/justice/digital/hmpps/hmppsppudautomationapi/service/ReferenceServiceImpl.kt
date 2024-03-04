package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient

@Component
@RequestScope
internal class ReferenceServiceImpl(
  private val ppudClient: ReferenceDataPpudClient,
  private val cacheManager: CacheManager,
) : ReferenceService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    const val CUSTODY_TYPES_CACHE_NAME: String = "CustodyTypes"
    const val ESTABLISHMENTS_CACHE_NAME: String = "Establishments"
    const val ETHNICITIES_CACHE_NAME: String = "Ethnicities"
    const val GENDERS_CACHE_NAME: String = "Genders"
    const val INDEX_OFFENCES_CACHE_NAME: String = "IndexOffences"
    const val MAPPA_LEVELS_CACHE_NAME: String = "MappaLevels"
    const val POLICE_FORCES_CACHE_NAME: String = "PoliceForces"
    const val PROBATION_SERVICES_CACHE_NAME: String = "ProbationServices"
    const val RELEASED_UNDERS_CACHE_NAME: String = "ReleasedUnders"
  }

  override fun clearCaches() {
    cacheManager.cacheNames.forEach {
      cacheManager.getCache(it)?.clear()
    }
  }

  override suspend fun refreshCaches() {
    LookupName.entries.forEach {
      refreshReferenceData(it)
    }
  }

  @Cacheable(CUSTODY_TYPES_CACHE_NAME)
  override suspend fun retrieveCustodyTypes(): List<String> {
    log.info("Retrieving '$CUSTODY_TYPES_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.CustodyTypes)
  }

  @Cacheable(ESTABLISHMENTS_CACHE_NAME)
  override suspend fun retrieveEstablishments(): List<String> {
    log.info("Retrieving '$ESTABLISHMENTS_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.Establishments)
  }

  @Cacheable(ETHNICITIES_CACHE_NAME)
  override suspend fun retrieveEthnicities(): List<String> {
    log.info("Retrieving '$ETHNICITIES_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.Ethnicities)
  }

  @Cacheable(GENDERS_CACHE_NAME)
  override suspend fun retrieveGenders(): List<String> {
    log.info("Retrieving '$GENDERS_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.Genders)
  }

  @Cacheable(INDEX_OFFENCES_CACHE_NAME)
  override suspend fun retrieveIndexOffences(): List<String> {
    log.info("Retrieving '$INDEX_OFFENCES_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.IndexOffences)
  }

  @Cacheable(MAPPA_LEVELS_CACHE_NAME)
  override suspend fun retrieveMappaLevels(): List<String> {
    log.info("Retrieving '$MAPPA_LEVELS_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.MappaLevels)
  }

  @Cacheable(POLICE_FORCES_CACHE_NAME)
  override suspend fun retrievePoliceForces(): List<String> {
    log.info("Retrieving '$POLICE_FORCES_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.PoliceForces)
  }

  @Cacheable(PROBATION_SERVICES_CACHE_NAME)
  override suspend fun retrieveProbationServices(): List<String> {
    log.info("Retrieving '$PROBATION_SERVICES_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.ProbationServices)
  }

  @Cacheable(RELEASED_UNDERS_CACHE_NAME)
  override suspend fun retrieveReleasedUnders(): List<String> {
    log.info("Retrieving '$RELEASED_UNDERS_CACHE_NAME'")
    return ppudClient.retrieveLookupValues(LookupName.ReleasedUnders)
  }

  private suspend fun refreshReferenceData(lookupName: LookupName) {
    log.info("Refreshing '$lookupName'")
    val values = ppudClient.retrieveLookupValues(lookupName)
    cacheManager.getCache(lookupName.name)?.put(SimpleKey.EMPTY, values) ?: throw RuntimeException("Cache '${lookupName.name}' not found")
  }
}
