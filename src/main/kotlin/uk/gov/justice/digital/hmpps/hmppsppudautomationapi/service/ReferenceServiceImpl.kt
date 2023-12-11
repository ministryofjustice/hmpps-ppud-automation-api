package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient

@Component
@RequestScope
internal class ReferenceServiceImpl(private val ppudClient: PpudClient) : ReferenceService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    const val ESTABLISHMENTS_CACHE_KEY: String = "establishments"
    const val ETHNICITIES_CACHE_KEY: String = "ethnicities"
    const val GENDERS_CACHE_KEY: String = "genders"
  }

  @Cacheable(ESTABLISHMENTS_CACHE_KEY)
  override suspend fun retrieveEstablishments(): List<String> {
    log.info("Retrieving '$ESTABLISHMENTS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Establishment)
  }

  @Cacheable(ETHNICITIES_CACHE_KEY)
  override suspend fun retrieveEthnicities(): List<String> {
    log.info("Retrieving '$ETHNICITIES_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Ethnicity)
  }

  @Cacheable(GENDERS_CACHE_KEY)
  override suspend fun retrieveGenders(): List<String> {
    log.info("Retrieving '$GENDERS_CACHE_KEY'")
    return ppudClient.retrieveLookupValues(LookupName.Gender)
  }
}
