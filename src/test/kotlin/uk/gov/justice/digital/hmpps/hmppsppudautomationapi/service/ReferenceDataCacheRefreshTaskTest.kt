package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then

@ExtendWith(MockitoExtension::class)
internal class ReferenceDataCacheRefreshTaskTest {

  @Mock
  private lateinit var referenceService: ReferenceService

  private lateinit var task: ReferenceDataCacheRefreshTask

  @BeforeEach
  fun beforeEach() {
    task = ReferenceDataCacheRefreshTask(referenceService)
  }

  @Test
  fun `given task is scheduled when triggered then refresh adn quit are invoked on reference service`() {
    runBlocking {
      task.performTask()
      then(referenceService).should().refreshCaches()
      then(referenceService).should().quit()
    }
  }

  @Test
  fun `given task is scheduled and an error occurs when triggered then quit is invoked on reference service`() {
    runBlocking {
      given(referenceService.refreshCaches()).willThrow(RuntimeException("Test exception"))
      assertThrows<RuntimeException> {
        task.performTask()
      }
      then(referenceService).should().quit()
    }
  }
}
