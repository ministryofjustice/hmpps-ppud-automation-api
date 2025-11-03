package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import ch.qos.logback.classic.Level
import io.flipt.client.FliptClient
import io.flipt.client.FliptException
import io.flipt.client.models.BooleanEvaluationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatcher
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.FliptConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag.FeatureFlagService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag.FeatureFlagService.FeatureFlagException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomBoolean
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.findLogAppender
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class FeatureFlagServiceTest {

  @InjectMocks
  private lateinit var featureFlagService: FeatureFlagService

  @Mock
  private lateinit var fliptClient: FliptClient

  @Mock
  private lateinit var fliptConfig: FliptConfig

  private val logAppender = findLogAppender(FeatureFlagService::class.java)

  @Test
  fun `returns true if feature flag is enabled`() {
    val flag = randomString()
    withFlag(flag, true)
    assertTrue(featureFlagService.enabled(flag))
  }

  @Test
  fun `returns false if feature flag is not enabled`() {
    val flag = randomString()
    withFlag(flag, false)
    assertFalse(featureFlagService.enabled(flag))
  }

  @Test
  fun `throws error if feature flag is not defined`() {
    val flag = randomString()
    whenever(fliptClient.evaluateBoolean(eq(flag), eq(flag), argThat(hasRecentCurrentDateTime()))).thenThrow(
      FliptException.EvaluationException("Not Found"),
    )
    assertThrows<FeatureFlagException> { featureFlagService.enabled(flag) }
  }

  @Test
  fun `logs warning and returns configured default if flipt client is not configured`() {
    val defaultFlagValue = randomBoolean()
    whenever(fliptConfig.defaultFlagValue).thenReturn(defaultFlagValue)

    // Simulate null Flipt client
    val featureFlagServiceWithNullClient = FeatureFlagService(null, fliptConfig)

    val isEnabled = featureFlagServiceWithNullClient.enabled("any-flag")

    assertThat(isEnabled).isEqualTo(defaultFlagValue)
    assertWarningMessageWasLogged(defaultFlagValue)
  }

  private fun withFlag(key: String, enabled: Boolean) {
    whenever(fliptClient.evaluateBoolean(eq(key), eq(key), argThat(hasRecentCurrentDateTime())))
      .thenReturn(
        flag(key, enabled),
      )
  }

  private fun flag(key: String, enabled: Boolean) = BooleanEvaluationResponse
    .builder()
    .enabled(enabled)
    .flagKey(key)
    .reason("DEFAULT_EVALUATION_REASON")
    .requestDurationMillis(100F)
    .timestamp(LocalTime.now().toString())
    .build()

  private fun assertWarningMessageWasLogged(defaultFlagValue: Boolean) {
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.WARN)
        assertThat(message).isEqualTo("Flipt client not configured, returning default value: $defaultFlagValue")
      }
    }
  }

  private fun hasRecentCurrentDateTime() = object : ArgumentMatcher<Map<String, String?>> {
    override fun matches(actualMap: Map<String, String?>): Boolean {
      val mappedCurrentDateTime = actualMap["currentDateTime"]
      if (mappedCurrentDateTime == null) {
        return false
      }
      // Check that the map contains the currentDateTime key with the expected format within a small window
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val mappedDateTime = LocalDateTime.parse(mappedCurrentDateTime, dateTimeFormatter)
      return LocalDateTime.now().isBefore(mappedDateTime.plusSeconds(3))
    }
  }
}
