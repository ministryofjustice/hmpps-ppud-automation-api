package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class HmppsPpudAutomationApiExceptionHandlerTest {
  @Test
  fun `given invalid field when handling MethodArgumentNotValidException then user message contains failure`() {

    val handler = HmppsPpudAutomationApiExceptionHandler()

    val errors = listOf(FieldError("", "fieldName", "Cannot be null"))
    val ex = Mockito.mock(MethodArgumentNotValidException::class.java)
    whenever(ex.allErrors).thenReturn(errors)

    val response = handler.handleMethodArgumentNotValidException(ex)

    assertThat(response.body?.userMessage).isEqualTo("Validation failure: fieldName: Cannot be null")
  }

  @Test
  fun `given multiple invalid fields when handling MethodArgumentNotValidException then user message contains failures`() {

    val handler = HmppsPpudAutomationApiExceptionHandler()

    val errors = listOf(
      FieldError("", "fieldName1", "Cannot be null"),
      FieldError("", "fieldName2", "Must be less than 10"),
    )
    val ex = Mockito.mock(MethodArgumentNotValidException::class.java)
    whenever(ex.allErrors).thenReturn(errors)

    val response = handler.handleMethodArgumentNotValidException(ex)

    assertThat(response.body?.userMessage).isEqualTo("Validation failure: fieldName1: Cannot be null, fieldName2: Must be less than 10")
  }
}
