package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(SpringExtension::class)
@ContextConfiguration
class ReferenceServiceTest {

  @MockBean
  private lateinit var ppudClient: PpudClient

  @Autowired
  private lateinit var service: ReferenceService

  @Autowired
  private lateinit var cache: CacheManager

  companion object {
    val cacheNames = arrayOf(
      "custody-types",
      "establishments",
      "ethnicities",
      "genders",
      "index-offences",
      "mappa-levels",
      "police-forces",
      "probation-services",
      "released-unders",
    )
  }

  @EnableCaching
  @TestConfiguration
  internal class CachingTestConfig {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(*Companion.cacheNames)

    @Bean
    fun referenceService(ppudClient: PpudClient, cacheManager: CacheManager): ReferenceService =
      ReferenceServiceImpl(ppudClient, cacheManager)
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
  fun `given caching when retrieveCustodyTypes called then custody types retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("custody-types", LookupName.CustodyTypes) { service.retrieveCustodyTypes() }
    }
  }

  @Test
  fun `given caching when retrieveEstablishments called then establishments retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("establishments", LookupName.Establishments) { service.retrieveEstablishments() }
    }
  }

  @Test
  fun `given caching when retrieveEthnicities called then ethnicities retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("ethnicities", LookupName.Ethnicities) { service.retrieveEthnicities() }
    }
  }

  @Test
  fun `given caching when retrieveGenders called then genders retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("genders", LookupName.Genders) { service.retrieveGenders() }
    }
  }

  @Test
  fun `given caching when retrieveIndexOffences called then index offences retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("index-offences", LookupName.IndexOffences) { service.retrieveIndexOffences() }
    }
  }

  @Test
  fun `given caching when retrieveMappaLevels called then mappa levels retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("mappa-levels", LookupName.MappaLevels) { service.retrieveMappaLevels() }
    }
  }

  @Test
  fun `given caching when retrievePoliceForces called then police forces retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("police-forces", LookupName.PoliceForces) { service.retrievePoliceForces() }
    }
  }

  @Test
  fun `given caching when retrieveProbationServices called then probation services retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached(
        "probation-services",
        LookupName.ProbationServices,
      ) { service.retrieveProbationServices() }
    }
  }

  @Test
  fun `given caching when retrieveReleasedUnders called then released unders retrieved and cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("released-unders", LookupName.ReleasedUnders) { service.retrieveReleasedUnders() }
    }
  }

  private suspend fun testValuesAreRetrievedAndCached(
    cacheKey: String,
    lookupName: LookupName,
    retrieve: suspend () -> List<String>,
  ) {
    val values = listOf(randomString(), randomString())
    given(ppudClient.retrieveLookupValues(lookupName)).willReturn(values)

    val valuesCacheMiss = retrieve()
    val valuesCacheHit = retrieve()

    assertEquals(values, valuesCacheMiss, "Values not retrieved from initial call")
    assertEquals(values, valuesCacheHit, "Values not retrieved from subsequent call")
    assertEquals(values, cache.getCache(cacheKey)?.get(SimpleKey.EMPTY)?.get(), "Values not present in cache")
    then(ppudClient).should(times(1)).retrieveLookupValues(lookupName)
  }
}
