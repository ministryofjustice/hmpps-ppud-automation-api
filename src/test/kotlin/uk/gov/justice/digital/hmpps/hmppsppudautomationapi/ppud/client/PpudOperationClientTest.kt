package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.openqa.selenium.WebDriverException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.err.PpudErrorHandlerClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
class PpudOperationClientTest {

  @InjectMocks
  private lateinit var operationClient: PpudOperationClient

  @Mock
  private lateinit var errorHandler: PpudErrorHandlerClient

  // TODO MRD-2769 find out why log testing fails
//  private val logAppender = findLogAppender(PpudOperationClient::class.java)

  @Test
  fun `invokes successfully on first try`() {
    runTest {
      // given
      val operation: Supplier<Any> = mock<Supplier<Any>>()
      val any = Any()
      given(operation.get()).willReturn(any)

      // when
      val returnedValue: Any = operationClient.invoke(false, operation)

      // then
      assertThat(returnedValue).isSameAs(any)
    }
  }

  @Test
  fun `invokes successfully on second try`() {
    runTest {
      // given
      val operation: Supplier<Any> = mock<Supplier<Any>>()
      val operationException = WebDriverException()
      val any = Any()
      given(operation.get()).willThrow(operationException).willReturn(any)

      // We give it a random string as the message to check it is indeed being logged,
      // as the log appender doesn't give us access to the logged exception
      val handlingException = AutomationException(randomString())
      given(errorHandler.handleException(operationException)).willReturn(handlingException)

      // when
      val returnedValue: Any = operationClient.invoke(true, operation)

      // then
      assertThat(returnedValue).isSameAs(any)
      verify(operation, times(2)).get()

      // TODO MRD-2769 find out why log testing fails
//      with(logAppender.list) {
//        assertThat(size).isEqualTo(1)
//        with(get(0)) {
//          assertThat(level).isEqualTo(Level.ERROR)
//          assertThat(message).isEqualTo("Exception occurred but operation will be retried")
//          assertThat(throwableProxy.message).isEqualTo(handlingException.message)
//        }
//      }
    }
  }

  @Test
  fun `bubbles up first failure if not retrying`() {
    runTest {
      // given
      val operation: Supplier<Any> = mock<Supplier<Any>>()
      val operationException: RuntimeException = RuntimeException()
      given(operation.get()).willThrow(operationException)

      // when
      val thrownException = assertThrows<RuntimeException> { operationClient.invoke(false, operation) }

      // then
      assertThat(thrownException).isSameAs(operationException)
      verify(operation, times(1)).get()
    }
  }

  @Test
  fun `bubbles up second failure if retrying`() {
    runTest {
      // given
      val operation: Supplier<Any> = mock<Supplier<Any>>()
      val firstOperationException = WebDriverException()
      val secondOperationException = WebDriverException()
      given(operation.get()).willThrow(firstOperationException).willThrow(secondOperationException)

      // We give it a random string as the message to check it is indeed being logged,
      // as the log appender doesn't give us access to the logged exception
      val handlingException = AutomationException(randomString())
      given(errorHandler.handleException(firstOperationException)).willReturn(handlingException)

      // when
      val thrownException = assertThrows<RuntimeException> { operationClient.invoke(true, operation) }

      // then
      assertThat(thrownException).isSameAs(secondOperationException)
      verify(operation, times(2)).get()

      // TODO MRD-2769 find out why log testing fails
//      with(logAppender.list) {
//        assertThat(size).isEqualTo(1)
//        with(get(0)) {
//          assertThat(level).isEqualTo(Level.ERROR)
//          assertThat(message).isEqualTo("Exception occurred but operation will be retried")
//          assertThat(throwableProxy.message).isEqualTo(handlingException.message)
//        }
//      }
    }
  }
}
