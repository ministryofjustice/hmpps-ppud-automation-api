package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag

enum class FeatureFlag(val flagId: String, val shouldTriggerCacheRefresh: Boolean) {
  PPUD_OCT_2025_ROLLOUT("PPUD-Oct-2025-Rollout", true)
}