package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
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

  @EnableCaching
  @TestConfiguration
  internal class CachingTestConfig {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager("establishments", "ethnicities", "genders")

    @Bean
    fun referenceService(ppudClient: PpudClient): ReferenceService = ReferenceServiceImpl(ppudClient)
  }

  @Test
  fun `given caching when retrieveEstablishments called then establishments cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("establishments", LookupName.Establishment) { service.retrieveEstablishments() }
    }
  }

  @Test
  fun `given caching when retrieveEthnicities called then ethnicities cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("ethnicities", LookupName.Ethnicity) { service.retrieveEthnicities() }
    }
  }

  @Test
  fun `given caching when retrieveGenders called then genders cached`() {
    runBlocking {
      testValuesAreRetrievedAndCached("genders", LookupName.Gender) { service.retrieveGenders() }
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

    assertEquals(values, valuesCacheMiss)
    assertEquals(values, valuesCacheHit)
    assertEquals(values, cache.getCache(cacheKey)?.get(SimpleKey.EMPTY)?.get())
    then(ppudClient).should(times(1)).retrieveLookupValues(lookupName)
  }
}
