package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.DEFAULT_POLLING_INTERVAL_IN_SECONDS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceService
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
internal class FeatureFlagTrackingService {

  @Autowired
  private lateinit var featureFlagService: FeatureFlagService

  @Autowired
  @Qualifier("scheduledReferenceService")
  private lateinit var referenceService: ReferenceService

  // For now, feature flags are simple booleans, so we don't need the map to be more complex
  private val lastRecordedFlagValues: MutableMap<FeatureFlag, Boolean> = mutableMapOf()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  // We want to check for changes in the flags as frequently as we poll, so we
  // know about the changes as soon as the Flipt client does (or close enough)
  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    fixedRateString = "\${flipt.polling-interval-in-seconds:${DEFAULT_POLLING_INTERVAL_IN_SECONDS}}",
  )
  suspend fun trackFeatureFlags() {
    FeatureFlag.entries.forEach { featureFlag ->
      val currentValue = featureFlagService.enabled(featureFlag.flagId)
      val lastValue = lastRecordedFlagValues[featureFlag]
      if (lastValue == null) {
        // First time seeing this flag, just record the value
        lastRecordedFlagValues[featureFlag] = currentValue
        log.info("New flag value tracked: $featureFlag is set to $currentValue")
      } else if (lastValue != currentValue) {
        if (featureFlag.shouldTriggerCacheRefresh) {
          // Introduce a small random delay before refreshing the caches to avoid
          // thundering herd issues between the multiple instances of the application
          val randomNumberOfSeconds = (0..10).random()
          delay(randomNumberOfSeconds.toDuration(DurationUnit.SECONDS))

          // Note there is technically a race condition occurring between this branch of the code and the refresh code
          // itself (at the time of writing happening in the EditLookupsPage class). The latter class also checks the
          // flag value in order to pick the right web element. Were the flag value have changed between the time we
          // retrieved it further up in this method and the time EditLookupsPage checks it, then it could pick the wrong
          // element. This can more easily happen given the refreshing code takes a long time to run, and is more likely
          // the later in the LookupName enumeration we get. However, the likelihood of this happening is very low,
          // given it would require someone to be flipping the flag state very frequently, and the impact is low too, as
          // it the second flip would be picked up again by the tracking service in the next polling cycle
          referenceService.refreshCaches()
        }
        lastRecordedFlagValues[featureFlag] = currentValue
        log.info("Flag value changed: $featureFlag changed to $currentValue")
      }
    }
  }
}
