package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
internal class ReferenceDataCacheRefreshTask(
  @Qualifier("scheduledReferenceService") private val referenceService: ReferenceService,
) {
  suspend fun performTask() {
    try {
      referenceService.refreshCaches()
    } finally {
      referenceService.quit()
    }
  }
}
