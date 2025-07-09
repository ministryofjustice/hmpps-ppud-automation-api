package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.recall

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.createdRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.createRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.recall.RecallClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
internal class RecallServiceTest {

  @InjectMocks
  private lateinit var recallService: RecallService

  @Mock
  private lateinit var authClient: PpudAuthClient

  @Mock
  private lateinit var recallClient: RecallClient

  // TODO MRD-2769 find out why log testing fails
//  private val logAppender = findLogAppender(RecallService::class.java)

  @Test
  fun `creates a recall`() {
    runTest {
      // given
      val offenderId = randomString()
      val sentenceId = randomString()
      val request = createRecallRequest()
      val methodCaptor = argumentCaptor<Supplier<CreatedRecall>>()

      val expectedCreatedOrUpdatedRelease = createdRecall()
      given(
        authClient.performLoggedInOperation(
          eq(false),
          eq(true),
          any<Supplier<CreatedRecall>>(),
        ),
      ).willReturn(
        expectedCreatedOrUpdatedRelease,
      )

      // when
      val actualCreatedOrUpdatedRelease = recallService.createRecall(offenderId, sentenceId, request)

      // then
      assertThat(actualCreatedOrUpdatedRelease).isEqualTo(expectedCreatedOrUpdatedRelease)

      then(authClient).should().performLoggedInOperation(eq(false), eq(true), methodCaptor.capture())

      then(recallClient).shouldHaveNoInteractions()
      given(recallClient.createRecall(offenderId, sentenceId, request)).willReturn(
        expectedCreatedOrUpdatedRelease,
      )
      val method: Supplier<CreatedRecall> = methodCaptor.firstValue
      method.get()
      then(recallClient).should().createRecall(offenderId, sentenceId, request)

      // TODO MRD-2769 find out why log testing fails
//      with(logAppender.list) {
//        assertThat(size).isEqualTo(1)
//        with(get(0)) {
//          assertThat(level).isEqualTo(Level.INFO)
//          assertThat(message).isEqualTo("Creating recall in PPUD Client")
//        }
//      }
    }
  }
}
