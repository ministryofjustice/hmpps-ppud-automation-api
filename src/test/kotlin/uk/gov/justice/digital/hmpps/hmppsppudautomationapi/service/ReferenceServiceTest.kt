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
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager("establishments", "ethnicities")

    @Bean
    fun referenceService(ppudClient: PpudClient): ReferenceService = ReferenceServiceImpl(ppudClient)
  }

  @Test
  fun `given caching when retrieveEstablishments called then establishments cached`() {
    runBlocking {
      val values = listOf(randomString(), randomString())
      given(ppudClient.retrieveLookupValues("Establishment"))
        .willReturn(values)

      val valuesCacheMiss = service.retrieveEstablishments()
      val valuesCacheHit = service.retrieveEstablishments()

      assertEquals(values, valuesCacheMiss)
      assertEquals(values, valuesCacheHit)
      assertEquals(values, cache.getCache("establishments")?.get(SimpleKey.EMPTY)?.get())
      then(ppudClient).should(times(1)).retrieveLookupValues("Establishment")
    }
  }

  @Test
  fun `given caching when retrieveEthnicities called then ethnicities cached`() {
    runBlocking {
      val values = listOf(randomString(), randomString())
      given(ppudClient.retrieveLookupValues("Ethnicity"))
        .willReturn(values)

      val valuesCacheMiss = service.retrieveEthnicities()
      val valuesCacheHit = service.retrieveEthnicities()

      assertEquals(values, valuesCacheMiss)
      assertEquals(values, valuesCacheHit)
      assertEquals(values, cache.getCache("ethnicities")?.get(SimpleKey.EMPTY)?.get())
      then(ppudClient).should(times(1)).retrieveLookupValues("Ethnicity")
    }
  }
}
