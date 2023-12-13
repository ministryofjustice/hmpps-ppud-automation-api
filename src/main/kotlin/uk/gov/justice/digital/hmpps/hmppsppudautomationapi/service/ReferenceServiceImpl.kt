package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient

@Component
@RequestScope
internal class ReferenceServiceImpl(private val ppudClient: PpudClient, private val cacheManager: CacheManager) :
  ReferenceService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    const val CUSTODY_TYPES_CACHE_KEY: String = "custody-types"
    const val ESTABLISHMENTS_CACHE_KEY: String = "establishments"
    const val ETHNICITIES_CACHE_KEY: String = "ethnicities"
    const val GENDERS_CACHE_KEY: String = "genders"
    const val INDEX_OFFENCES_CACHE_KEY: String = "index-offences"
    const val MAPPA_LEVELS_CACHE_KEY: String = "mappa-levels"
    const val POLICE_FORCES_CACHE_KEY: String = "police-forces"
    const val PROBATION_SERVICES_CACHE_KEY: String = "probation-services"
    const val RELEASED_UNDERS_CACHE_KEY: String = "released-unders"
  }

  override fun clearCaches() {
    cacheManager.cacheNames.forEach {
      cacheManager.getCache(it)?.clear()
    }
  }

  @Cacheable(CUSTODY_TYPES_CACHE_KEY)
  override suspend fun retrieveCustodyTypes(): List<String> {
    log.info("Retrieving '$CUSTODY_TYPES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.CustodyTypes)
  }

  @Cacheable(ESTABLISHMENTS_CACHE_KEY)
  override suspend fun retrieveEstablishments(): List<String> {
    log.info("Retrieving '$ESTABLISHMENTS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Establishments)
  }

  @Cacheable(ETHNICITIES_CACHE_KEY)
  override suspend fun retrieveEthnicities(): List<String> {
    log.info("Retrieving '$ETHNICITIES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Ethnicities)
  }

  @Cacheable(GENDERS_CACHE_KEY)
  override suspend fun retrieveGenders(): List<String> {
    log.info("Retrieving '$GENDERS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Genders)
  }

  @Cacheable(INDEX_OFFENCES_CACHE_KEY)
  override suspend fun retrieveIndexOffences(): List<String> {
    log.info("Retrieving '$INDEX_OFFENCES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.IndexOffences)
  }

  @Cacheable(MAPPA_LEVELS_CACHE_KEY)
  override suspend fun retrieveMappaLevels(): List<String> {
    log.info("Retrieving '$MAPPA_LEVELS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.MappaLevels)
  }

  @Cacheable(POLICE_FORCES_CACHE_KEY)
  override suspend fun retrievePoliceForces(): List<String> {
    log.info("Retrieving '$POLICE_FORCES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.PoliceForces)
  }

  @Cacheable(PROBATION_SERVICES_CACHE_KEY)
  override suspend fun retrieveProbationServices(): List<String> {
    log.info("Retrieving '$PROBATION_SERVICES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.ProbationServices)
  }

  @Cacheable(RELEASED_UNDERS_CACHE_KEY)
  override suspend fun retrieveReleasedUnders(): List<String> {
    log.info("Retrieving '$RELEASED_UNDERS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.ReleasedUnders)
  }
}
