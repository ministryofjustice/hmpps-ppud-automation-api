package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.CacheConstants.ESTABLISHMENTS_CACHE_KEY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient

@Component
@RequestScope
internal class ReferenceServiceImpl(private val ppudClient: PpudClient) : ReferenceService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Cacheable(ESTABLISHMENTS_CACHE_KEY)
  override suspend fun retrieveEstablishments(): List<String> {
    log.info("Retrieving '$ESTABLISHMENTS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues()
  }
}
