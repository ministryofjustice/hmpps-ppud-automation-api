package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.openqa.selenium.WebElement
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class WebDriverHelperTest {

  @Mock
  private lateinit var element: WebElement

  private val nullElement: WebElement? = null

  @ParameterizedTest
  @CsvSource(
    "simple,simple",
    "'    padLeft',padLeft",
    "'padRight   ',padRight",
    "'  padBoth  ',padBoth",
  )
  fun `given element has a value when getValue is called then trimmed value is returned`(
    value: String,
    expected: String,
  ) {
    given(element.getAttribute("value")).willReturn(value)
    val actual = element.getValue()
    assertEquals(expected, actual)
  }

  @Test
  fun `given element is null when getValue is called then empty string is returned`() {
    val actual = nullElement.getValue()
    assertEquals("", actual)
  }

  @Test
  fun `given element has no value when getValue is called then empty string is returned`() {
    given(element.getAttribute("value")).willReturn(null)
    val actual = element.getValue()
    assertEquals("", actual)
  }

  @Test
  fun `given some text when enterTextIfNotBlank is called then text is sent`() {
    val text = randomString("text")
    element.enterTextIfNotBlank(text)
    then(element).should().sendKeys(text)
  }

  @Test
  fun `given element is null when enterTextIfNotBlank is called then function succeeds`() {
    assertDoesNotThrow { nullElement.enterTextIfNotBlank(randomString()) }
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["  ", "\t", "\n"])
  fun `given text is null or blank when enterTextIfNotBlank is called then text is not sent`(text: String?) {
    element.enterTextIfNotBlank(text)
    then(element).should(never()).sendKeys(any())
  }

  @ParameterizedTest
  @NullAndEmptySource
  fun `given a null or blank dropdown option value when selectDropdownOptionIfNotBlank is called then option is not selected`(option: String?) {
    selectDropdownOptionIfNotBlank(element, option)
    then(element).shouldHaveNoInteractions()
  }
}
