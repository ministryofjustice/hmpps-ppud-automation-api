package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.DETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.INDETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient

@Component
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
    const val COURTS_CACHE_NAME: String = "Courts"
    const val DETERMINATE_CUSTODY_TYPES_CACHE_NAME: String = "DeterminateCustodyTypes"
    const val INDETERMINATE_CUSTODY_TYPES_CACHE_NAME: String = "IndeterminateCustodyTypes"
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

    // We need to do these separately, as they don't have one-to-one
    // relationships to lookup names, so aren't covered by the code above
    cacheManager.getCache(DETERMINATE_CUSTODY_TYPES_CACHE_NAME)?.put(SimpleKey.EMPTY, retrieveDeterminateCustodyTypes())
      ?: throw RuntimeException("Cache '$DETERMINATE_CUSTODY_TYPES_CACHE_NAME' not found")
    cacheManager.getCache(INDETERMINATE_CUSTODY_TYPES_CACHE_NAME)
      ?.put(SimpleKey.EMPTY, retrieveIndeterminateCustodyTypes())
      ?: throw RuntimeException("Cache '$INDETERMINATE_CUSTODY_TYPES_CACHE_NAME' not found")
  }

  @Cacheable(CUSTODY_TYPES_CACHE_NAME)
  override suspend fun retrieveCustodyTypes(): List<String> = ppudClient.retrieveLookupValues(LookupName.CustodyTypes)

  @Cacheable(ESTABLISHMENTS_CACHE_NAME)
  override suspend fun retrieveEstablishments(): List<String> = ppudClient.retrieveLookupValues(LookupName.Establishments)

  @Cacheable(ETHNICITIES_CACHE_NAME)
  override suspend fun retrieveEthnicities(): List<String> = ppudClient.retrieveLookupValues(LookupName.Ethnicities)

  @Cacheable(GENDERS_CACHE_NAME)
  override suspend fun retrieveGenders(): List<String> = ppudClient.retrieveLookupValues(LookupName.Genders)

  @Cacheable(INDEX_OFFENCES_CACHE_NAME)
  override suspend fun retrieveIndexOffences(): List<String> = ppudClient.retrieveLookupValues(LookupName.IndexOffences)

  @Cacheable(MAPPA_LEVELS_CACHE_NAME)
  override suspend fun retrieveMappaLevels(): List<String> = ppudClient.retrieveLookupValues(LookupName.MappaLevels)

  @Cacheable(POLICE_FORCES_CACHE_NAME)
  override suspend fun retrievePoliceForces(): List<String> = ppudClient.retrieveLookupValues(LookupName.PoliceForces)

  @Cacheable(PROBATION_SERVICES_CACHE_NAME)
  override suspend fun retrieveProbationServices(): List<String> = ppudClient.retrieveLookupValues(LookupName.ProbationServices)

  @Cacheable(RELEASED_UNDERS_CACHE_NAME)
  override suspend fun retrieveReleasedUnders(): List<String> = ppudClient.retrieveLookupValues(LookupName.ReleasedUnders)

  @Cacheable(COURTS_CACHE_NAME)
  override suspend fun retrieveCourts(): List<String> = ppudClient.retrieveLookupValues(LookupName.Courts)

  @Cacheable(DETERMINATE_CUSTODY_TYPES_CACHE_NAME)
  override suspend fun retrieveDeterminateCustodyTypes(): List<String> = retrieveCustodyTypesByCustodyGroup(DETERMINATE)

  @Cacheable(INDETERMINATE_CUSTODY_TYPES_CACHE_NAME)
  override suspend fun retrieveIndeterminateCustodyTypes(): List<String> = retrieveCustodyTypesByCustodyGroup(INDETERMINATE)

  override fun quit() {
    ppudClient.quit()
  }

  private suspend fun refreshReferenceData(lookupName: LookupName) {
    log.info("Refreshing cache '$lookupName'")
    val values = ppudClient.retrieveLookupValues(lookupName)
    cacheManager.getCache(lookupName.name)?.put(SimpleKey.EMPTY, values)
      ?: throw RuntimeException("Cache '${lookupName.name}' not found")
  }

  private suspend fun retrieveCustodyTypesByCustodyGroup(
    custodyGroup: CustodyGroup,
  ): List<String> {
    // This call bypasses the cache. We could fix this by having this service get an Autowired reference
    // to itself (which would give us the cache-wrapped version of itself), but given the consumers of this
    // method are also caching, it'll have very little impact on overall performance, so we don't fix it
    val allCustodyTypes = retrieveCustodyTypes()

    val availableDeterminateCustodyTypes: List<SupportedCustodyType> = allCustodyTypes.mapNotNull {
      try {
        SupportedCustodyType.forFullName(it)
      } catch (ex: NoSuchElementException) {
        // do nothing - we have encountered a type we don't recognise/support, so we leave it out
        null
      }
    }.filter { it.custodyGroup == custodyGroup }

    val supportedDeterminateCustodyTypes = SupportedCustodyType.entries.filter { it.custodyGroup == custodyGroup }
    supportedDeterminateCustodyTypes
      .filterNot { availableDeterminateCustodyTypes.contains(it) }
      .forEach {
        log.warn("${custodyGroup.fullName} custody type not found in PPUD: ${it.fullName}")
      }

    return availableDeterminateCustodyTypes.map { it.fullName }
  }
}
