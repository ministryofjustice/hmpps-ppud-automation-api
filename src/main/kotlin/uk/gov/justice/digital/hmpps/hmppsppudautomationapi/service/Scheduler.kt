package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
internal class Scheduler(
  private val referenceDataCacheRefreshTask: ReferenceDataCacheRefreshTask,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    initialDelayString = "\${cache.refreshInitialDelaySeconds}",
    fixedDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(\${cache.timeToLiveSeconds}/2,\${cache.timeToLiveSeconds}) }",
  )
  suspend fun refreshCaches() {
    log.info("Reference Data Cache Refresh triggered")
    referenceDataCacheRefreshTask.performTask()
  }
}
