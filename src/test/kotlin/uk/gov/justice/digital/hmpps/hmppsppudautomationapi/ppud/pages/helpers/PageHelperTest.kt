package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.enterTextIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class PageHelperTest {

  @Mock
  private lateinit var element: WebElement

  @Mock
  private lateinit var dateFormatter: DateTimeFormatter

  private lateinit var pageHelper: PageHelper

  @BeforeEach
  fun beforeEach() {
    pageHelper = PageHelper(dateFormatter)
  }

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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["  ", "\t", "\n"])
  fun `given text is null or blank when enterTextIfNotBlank is called then text is not sent`(text: String?) {
    element.enterTextIfNotBlank(text)
    then(element).should(never()).sendKeys(any())
  }

  @ParameterizedTest
  @NullAndEmptySource
  fun `given a null or blank dropdown option value when selectDropdownOptionIfNotBlank is called then option is not selected`(
    option: String?,
  ) {
    pageHelper.selectDropdownOptionIfNotBlank(element, option, "")
    then(element).shouldHaveNoInteractions()
  }

  @Test
  fun `given an unmatched dropdown option value when selectDropdownOptionIfNotBlank is called then friendly exception is thrown`() {
    given(element.tagName).willReturn("select")
    given(element.isEnabled).willReturn(true)
    val option = randomString()
    val description = randomString()
    val exception = assertThrows<AutomationException> {
      pageHelper.selectDropdownOptionIfNotBlank(element, option, description)
    }
    assertEquals("Cannot locate $description option with text '$option'", exception.message)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `given a value different to the current state when selectCheckboxValue is called then checkbox is clicked`(
    toBeChecked: Boolean,
  ) {
    given(element.isSelected).willReturn(!toBeChecked)
    pageHelper.selectCheckboxValue(element, toBeChecked)
    then(element).should().click()
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `given a value same as the current state when selectCheckboxValue is called then checkbox is not clicked`(
    toBeChecked: Boolean,
  ) {
    given(element.isSelected).willReturn(toBeChecked)
    pageHelper.selectCheckboxValue(element, toBeChecked)
    then(element).should(never()).click()
  }
}
