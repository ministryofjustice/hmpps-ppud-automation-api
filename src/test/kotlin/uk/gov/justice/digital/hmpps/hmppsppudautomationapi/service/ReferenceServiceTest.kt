package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient
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
      "CustodyTypes",
      "Establishments",
      "Ethnicities",
      "Genders",
      "IndexOffences",
      "MappaLevels",
      "PoliceForces",
      "ProbationServices",
      "ReleasedUnders",
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
        cache.getCache(cacheName)?.put(SimpleKey.EMPTY, listOf(randomString()))
      }
      given(ppudClient.retrieveLookupValues(any())).will { listOf(randomString(it.getArgument<LookupName>(0).name)) }

      service.refreshCaches()

      for (cacheName in cacheNames) {
        val refreshedCacheValues = cache.getCache(cacheName)?.get(SimpleKey.EMPTY)?.get()
        Assertions.assertNotNull(refreshedCacheValues, "$cacheName cache is empty")
        val firstRefreshedCacheValue = (refreshedCacheValues as List<*>).first().toString()
        Assertions.assertTrue(
          firstRefreshedCacheValue.startsWith(cacheName),
          "Expected retrieved value to start with $cacheName but value is $firstRefreshedCacheValue",
        )
      }
    }
  }

  @Test
  fun `given caching and empty caches when refreshCaches called then all caches are updated with freshly retrieved values`() {
    runBlocking {
      clearMockCaches()
      given(ppudClient.retrieveLookupValues(any())).will { listOf(randomString(it.getArgument<LookupName>(0).name)) }

      service.refreshCaches()

      for (cacheName in cacheNames) {
        val refreshedCacheValues = cache.getCache(cacheName)?.get(SimpleKey.EMPTY)?.get()
        Assertions.assertNotNull(refreshedCacheValues, "$cacheName cache is empty")
        val firstRefreshedCacheValue = (refreshedCacheValues as List<*>).first().toString()
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
