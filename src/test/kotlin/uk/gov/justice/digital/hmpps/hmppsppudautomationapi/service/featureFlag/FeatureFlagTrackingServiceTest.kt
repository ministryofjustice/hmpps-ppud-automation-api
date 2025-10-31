package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.featureFlag

import ch.qos.logback.classic.Level
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomBoolean
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.findLogAppender

@ExtendWith(MockitoExtension::class)
class FeatureFlagTrackingServiceTest {

  @InjectMocks
  private lateinit var featureFlagTrackingService: FeatureFlagTrackingService

  @Mock
  private lateinit var featureFlagService: FeatureFlagService

  @Mock
  private lateinit var referenceService: ReferenceService

  private val logAppender = findLogAppender(FeatureFlagTrackingService::class.java)

  @Test
  fun `should not trigger any refreshes when first tracking the flag value`() {
    runTest {
      // given
      val flagValues = mutableMapOf<FeatureFlag, Boolean>()
      FeatureFlag.entries.forEach {
        flagValues.put(it, randomBoolean())
        given(featureFlagService.enabled(it.flagId)).willReturn(flagValues[it])
      }

      // when
      featureFlagTrackingService.trackFeatureFlags()

      // then
      then(referenceService).shouldHaveNoInteractions()
      assertThat(logAppender.list).hasSameSizeAs(FeatureFlag.entries)
      with(logAppender.list) {
        FeatureFlag.entries.forEachIndexed { index, featureFlag ->
          with(get(index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("New flag value tracked: $featureFlag is set to ${flagValues[featureFlag]}")
          }
        }
      }
    }
  }

  @Test
  fun `should not trigger any refreshes when the flag values haven't changed`() {
    runTest {
      // given
      val flagValues = mutableMapOf<FeatureFlag, Boolean>()
      FeatureFlag.entries.forEach {
        flagValues[it] = randomBoolean()
        given(featureFlagService.enabled(it.flagId)).willReturn(flagValues[it])
      }
      // we call it once to record the initial values
      featureFlagTrackingService.trackFeatureFlags()

      // when
      featureFlagTrackingService.trackFeatureFlags()

      // then
      then(referenceService).shouldHaveNoInteractions()
      assertThat(logAppender.list).hasSameSizeAs(FeatureFlag.entries)
      with(logAppender.list) {
        FeatureFlag.entries.forEachIndexed { index, featureFlag ->
          with(get(index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("New flag value tracked: $featureFlag is set to ${flagValues[featureFlag]}")
          }
        }
      }
    }
  }

  @Test
  fun `should trigger a refresh when the flag values which should trigger a refresh have changed`() {
    runTest {
      // given
      val initialFlagValues = mutableMapOf<FeatureFlag, Boolean>()
      val updatedFlagValues = mutableMapOf<FeatureFlag, Boolean>()
      FeatureFlag.entries.forEach {
        initialFlagValues[it] = randomBoolean()
        if (it.shouldTriggerCacheRefresh) {
          updatedFlagValues[it] = !initialFlagValues[it]!!
          given(featureFlagService.enabled(it.flagId)).willReturn(initialFlagValues[it])
            .willReturn(updatedFlagValues[it])
        } else {
          given(featureFlagService.enabled(it.flagId)).willReturn(initialFlagValues[it])
        }
      }
      // we call it once to record the initial values
      featureFlagTrackingService.trackFeatureFlags()

      // when
      featureFlagTrackingService.trackFeatureFlags()

      // then
      then(referenceService).should().refreshCaches()
      with(logAppender.list) {
        assertThat(size).isEqualTo(FeatureFlag.entries.size + updatedFlagValues.size)
        FeatureFlag.entries.forEachIndexed { index, featureFlag ->
          with(get(index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("New flag value tracked: $featureFlag is set to ${initialFlagValues[featureFlag]}")
          }
        }
        updatedFlagValues.entries.forEachIndexed { index, featureFlagMapping ->
          with(get(FeatureFlag.entries.size + index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("Flag value changed: ${featureFlagMapping.key} changed to ${featureFlagMapping.value}")
          }
        }
      }
    }
  }

  @Test
  fun `should not trigger a refresh when the flag values which should trigger a refresh haven't changed`() {
    runTest {
      // given
      val initialFlagValues = mutableMapOf<FeatureFlag, Boolean>()
      val updatedFlagValues = mutableMapOf<FeatureFlag, Boolean>()
      FeatureFlag.entries.forEach {
        initialFlagValues[it] = randomBoolean()
        if (!it.shouldTriggerCacheRefresh) {
          updatedFlagValues[it] = !initialFlagValues[it]!!
          given(featureFlagService.enabled(it.flagId)).willReturn(initialFlagValues[it])
            .willReturn(updatedFlagValues[it])
        } else {
          given(featureFlagService.enabled(it.flagId)).willReturn(initialFlagValues[it])
        }
      }
      // we call it once to record the initial values
      featureFlagTrackingService.trackFeatureFlags()

      // when
      featureFlagTrackingService.trackFeatureFlags()

      // then
      then(referenceService).shouldHaveNoInteractions()
      with(logAppender.list) {
        assertThat(size).isEqualTo(FeatureFlag.entries.size + updatedFlagValues.size)
        FeatureFlag.entries.forEachIndexed { index, featureFlag ->
          with(get(index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("New flag value tracked: $featureFlag is set to ${initialFlagValues[featureFlag]}")
          }
        }
        updatedFlagValues.entries.forEachIndexed { index, featureFlagMapping ->
          with(get(FeatureFlag.entries.size + index)) {
            assertThat(level).isEqualTo(Level.INFO)
            assertThat(message).isEqualTo("Flag value changed: ${featureFlagMapping.key} changed to ${featureFlagMapping.value}")
          }
        }
      }
    }
  }

}