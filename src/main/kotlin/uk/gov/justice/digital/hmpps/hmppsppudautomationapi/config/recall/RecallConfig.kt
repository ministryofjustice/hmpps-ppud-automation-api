package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.recall

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class RecallConfig(
  @Value("\${ppud.recall.recallType.determinate}") val determinateRecallType: String,
  @Value("\${ppud.recall.recallType.indeterminate}") val indeterminateRecallType: String,
)
