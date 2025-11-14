package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.willReturn
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName.CustodyTypes
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.COURTS_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.CUSTODY_TYPES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.DETERMINATE_CUSTODY_TYPES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.ESTABLISHMENTS_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.ETHNICITIES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.GENDERS_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.INDETERMINATE_CUSTODY_TYPES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.INDEX_OFFENCES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.MAPPA_LEVELS_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.POLICE_FORCES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.PROBATION_SERVICES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.RELEASED_UNDERS_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(SpringExtension::class)
@ContextConfiguration
class ReferenceServiceTest {

  @MockBean
  private lateinit var ppudClient: ReferenceDataPpudClient

  @Autowired
  private lateinit var service: ReferenceService

  @Autowired
  private lateinit var cache: CacheManager

  companion object {
    val cacheNames = arrayOf(
      CUSTODY_TYPES_CACHE_NAME,
      ESTABLISHMENTS_CACHE_NAME,
      ETHNICITIES_CACHE_NAME,
      GENDERS_CACHE_NAME,
      INDEX_OFFENCES_CACHE_NAME,
      MAPPA_LEVELS_CACHE_NAME,
      POLICE_FORCES_CACHE_NAME,
      PROBATION_SERVICES_CACHE_NAME,
      RELEASED_UNDERS_CACHE_NAME,
      COURTS_CACHE_NAME,
      DETERMINATE_CUSTODY_TYPES_CACHE_NAME,
      INDETERMINATE_CUSTODY_TYPES_CACHE_NAME,
    )
  }

  @EnableCaching
  @TestConfiguration
  internal class CachingTestConfig {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*Companion.cacheNames)

    @Bean
    fun referenceService(ppudClient: ReferenceDataPpudClient, cacheManager: CacheManager): ReferenceService = ReferenceServiceImpl(ppudClient, cacheManager)
  }

  @Test
  fun `given caching when clearCaches called then all caches are cleared`() {
    for (cacheName in cacheNames) {
      cache.getCache(cacheName)?.put(SimpleKey.EMPTY, listOf(randomString()))
    }

    service.clearCaches()

    for (cacheName in cacheNames) {
      Assertions.assertNull(cache.getCache(cacheName)?.get(SimpleKey.EMPTY)?.get(), "$cacheName cache is not empty")
    }
  }

  @Test
  fun `given caching and populated caches when refreshCaches called then all caches are updated with freshly retrieved values`() {
    runBlocking {
      for (cacheName in cacheNames) {
        // We need to separate the custody type cases, as they filter out unrecognised values (such as random strings)
        val values = if (cacheName === DETERMINATE_CUSTODY_TYPES_CACHE_NAME) {
          listOf(SupportedCustodyType.EDS.fullName)
        } else if (cacheName === INDETERMINATE_CUSTODY_TYPES_CACHE_NAME) {
          listOf(SupportedCustodyType.AUTOMATIC.fullName)
        } else {
          listOf(randomString())
        }
        cache.getCache(cacheName)?.put(SimpleKey.EMPTY, values)
      }
      testCacheRefresh()
    }
  }

  @Test
  fun `given caching and empty caches when refreshCaches called then all caches are updated with freshly retrieved values`() {
    runBlocking {
      clearMockCaches()
      testCacheRefresh()
    }
  }

  private suspend fun testCacheRefresh() {
    // We need to separate the custody type cases, as they filter out unrecognised values (such as random strings)
    val cachedDeterminateCustodyType = SupportedCustodyType.DETERMINATE.fullName
    val cachedIndeterminateCustodyType = SupportedCustodyType.DPP.fullName
    willReturn(
      listOf(
        cachedDeterminateCustodyType,
        cachedIndeterminateCustodyType,
      ),
    ).given(ppudClient).retrieveLookupValues(CustodyTypes)
    willReturn(listOf(randomString())).given(ppudClient).retrieveLookupValues(any())
    given(ppudClient.retrieveLookupValues(any())).willAnswer {
      val cacheLookupName = it.getArgument<LookupName>(0)
      return@willAnswer if (cacheLookupName === CustodyTypes) {
        listOf(
          cachedDeterminateCustodyType,
          cachedIndeterminateCustodyType,
        )
      } else {
        listOf(randomString(cacheLookupName.name))
      }
    }

    service.refreshCaches()

    for (cacheName in cacheNames) {
      val refreshedCacheValues = cache.getCache(cacheName)?.get(SimpleKey.EMPTY)?.get()
      Assertions.assertNotNull(refreshedCacheValues, "$cacheName cache is empty")
      val firstRefreshedCacheValue = (refreshedCacheValues as List<*>).first().toString()

      if (cacheName in listOf(DETERMINATE_CUSTODY_TYPES_CACHE_NAME, CUSTODY_TYPES_CACHE_NAME)) {
        org.assertj.core.api.Assertions.assertThat(firstRefreshedCacheValue).isEqualTo(cachedDeterminateCustodyType)
      } else if (cacheName === INDETERMINATE_CUSTODY_TYPES_CACHE_NAME) {
        org.assertj.core.api.Assertions.assertThat(firstRefreshedCacheValue).isEqualTo(cachedIndeterminateCustodyType)
      } else {
        Assertions.assertTrue(
          firstRefreshedCacheValue.startsWith(cacheName),
          "Expected retrieved value to start with $cacheName but value is $firstRefreshedCacheValue",
        )
      }
    }
  }

  @Test
  fun `given caching when retrieveCustodyTypes called then custody types retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.CustodyTypes) { service.retrieveCustodyTypes() }
    }
  }

  @Test
  fun `given caching when retrieveEstablishments called then establishments retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.Establishments) { service.retrieveEstablishments() }
    }
  }

  @Test
  fun `given caching when retrieveEthnicities called then ethnicities retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.Ethnicities) { service.retrieveEthnicities() }
    }
  }

  @Test
  fun `given caching when retrieveGenders called then genders retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.Genders) { service.retrieveGenders() }
    }
  }

  @Test
  fun `given caching when retrieveIndexOffences called then index offences retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.IndexOffences) { service.retrieveIndexOffences() }
    }
  }

  @Test
  fun `given caching when retrieveMappaLevels called then mappa levels retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.MappaLevels) { service.retrieveMappaLevels() }
    }
  }

  @Test
  fun `given caching when retrievePoliceForces called then police forces retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.PoliceForces) { service.retrievePoliceForces() }
    }
  }

  @Test
  fun `given caching when retrieveProbationServices called then probation services retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.ProbationServices) { service.retrieveProbationServices() }
    }
  }

  @Test
  fun `given caching when retrieveReleasedUnders called then released unders retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.ReleasedUnders) { service.retrieveReleasedUnders() }
    }
  }

  @Test
  fun `given caching when retrieveCourts called then courts retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(LookupName.Courts) { service.retrieveCourts() }
    }
  }

  private suspend fun testValuesAreRetrievedAndCached(
    lookupName: LookupName,
    retrieve: suspend () -> List<String>,
  ) {
    clearMockCaches()
    val values = listOf(randomString(), randomString())
    given(ppudClient.retrieveLookupValues(lookupName)).willReturn(values)

    val valuesCacheMiss = retrieve()
    val valuesCacheHit = retrieve()

    assertEquals(values, valuesCacheMiss, "Values not retrieved from initial call")
    assertEquals(values, valuesCacheHit, "Values not retrieved from subsequent call")
    assertEquals(values, cache.getCache(lookupName.name)?.get(SimpleKey.EMPTY)?.get(), "Values not present in cache")
    then(ppudClient).should(times(1)).retrieveLookupValues(lookupName)
  }

  private fun clearMockCaches() {
    cache.cacheNames.forEach {
      cache.getCache(it)?.clear()
    }
  }
}
