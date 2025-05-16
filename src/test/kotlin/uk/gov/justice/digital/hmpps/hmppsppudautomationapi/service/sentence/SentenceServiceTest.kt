package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.sentence

import ch.qos.logback.classic.Level
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
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.createdSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.createOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.findLogAppender
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
internal class SentenceServiceTest {

  @InjectMocks
  private lateinit var sentenceService: SentenceService

  @Mock
  private lateinit var authClient: PpudAuthClient

  @Mock
  private lateinit var sentenceClient: SentenceClient

  private val logAppender = findLogAppender(SentenceService::class.java)

  @Test
  fun `creates a sentence`() {
    runTest {
      // given
      val offenderId = randomString()
      val sentenceRequest = createOrUpdateSentenceRequest()
      val methodCaptor = argumentCaptor<Supplier<CreatedSentence>>()

      val expectedCreatedSentence = createdSentence()
      given(authClient.performLoggedInOperation(eq(false), eq(true), any<Supplier<CreatedSentence>>())).willReturn(
        expectedCreatedSentence,
      )

      // when
      val actualCreatedSentence = sentenceService.createSentence(offenderId, sentenceRequest)

      // then
      assertThat(actualCreatedSentence).isEqualTo(expectedCreatedSentence)

      then(authClient).should().performLoggedInOperation(eq(false), eq(true), methodCaptor.capture())

      then(sentenceClient).shouldHaveNoInteractions()
      given(sentenceClient.createSentence(offenderId, sentenceRequest)).willReturn(expectedCreatedSentence)
      val method: Supplier<CreatedSentence> = methodCaptor.firstValue
      method.get()
      then(sentenceClient).should().createSentence(offenderId, sentenceRequest)

      with(logAppender.list) {
        assertThat(size).isEqualTo(1)
        with(get(0)) {
          assertThat(level).isEqualTo(Level.INFO)
          assertThat(message).isEqualTo("Creating sentence in PPUD Client")
        }
      }
    }
  }

  @Test
  fun `updates a sentence`() {
    runTest {
      // given
      val methodCaptor = argumentCaptor<Supplier<Unit>>()
      val offenderId = randomString()
      val sentenceId = randomString()
      val sentenceRequest = createOrUpdateSentenceRequest()

      given(authClient.performLoggedInOperation(eq(false), eq(true), any<Supplier<Unit>>())).willReturn(
        Unit,
      )

      // when
      sentenceService.updateSentence(offenderId, sentenceId, sentenceRequest)

      // then
      verify(authClient).performLoggedInOperation(eq(false), eq(true), methodCaptor.capture())
      val method: Supplier<Unit> = methodCaptor.firstValue
      method.get()
      verify(sentenceClient).updateSentence(offenderId, sentenceId, sentenceRequest)
      with(logAppender.list) {
        assertThat(size).isEqualTo(1)
        with(get(0)) {
          assertThat(level).isEqualTo(Level.INFO)
          assertThat(message).isEqualTo("Updating sentence in PPUD Client")
        }
      }
    }
  }
}
