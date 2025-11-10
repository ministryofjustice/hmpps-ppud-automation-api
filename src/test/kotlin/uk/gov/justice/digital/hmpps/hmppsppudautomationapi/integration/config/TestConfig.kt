package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.config

import io.flipt.client.FliptClient
import io.flipt.client.models.BooleanEvaluationResponse
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag.FeatureFlag

@TestConfiguration
class TestConfig {
  // We don't know and have no control over the exact calls the FliptClient will make to retrieve
  // the flag values, so instead of trying to wiremock the responses we mock the FliptClient itself
  @Bean
  @Primary
  fun fliptApiClientOverride(): FliptClient {
    val booleanResponse = mock(BooleanEvaluationResponse::class.java)
    whenever(booleanResponse.isEnabled).thenReturn(true)
    val fliptApiClient = mock(FliptClient::class.java)
    FeatureFlag.entries.forEach {
      whenever(fliptApiClient.evaluateBoolean(eq(it.flagId), eq(it.flagId), anyMap())).thenReturn(booleanResponse)
    }
    return fliptApiClient
  }
}
