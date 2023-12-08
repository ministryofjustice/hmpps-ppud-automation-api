package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient

@Component
internal class ReferenceService(private val ppudClient: PpudClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private const val cacheTimeToLiveInMs = 12 * 60 * 60 * 1000 // Empty cache every 12 hours

    private const val cacheNameEstablishments = "establishments"
  }

  @Cacheable(cacheNameEstablishments)
  suspend fun retrieveEstablishments(): List<String> {
    log.info("Retrieving '$cacheNameEstablishments'")
    return ppudClient.retrieveLookupValues()
  }

  @CacheEvict(value = [cacheNameEstablishments], allEntries = true)
  @Scheduled(fixedRateString = "$cacheTimeToLiveInMs")
  suspend fun emptyEstablishmentsCache() {
    log.info("Emptying cache of '$cacheNameEstablishments'")
  }
}
