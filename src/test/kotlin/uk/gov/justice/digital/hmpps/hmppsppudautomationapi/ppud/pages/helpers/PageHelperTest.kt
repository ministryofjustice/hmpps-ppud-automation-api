package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@ExtendWith(MockitoExtension::class)
class PageHelperTest {

  @Mock
  private lateinit var element: WebElement

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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
  fun `given a date when enterDate is called then date is sent to element`() {
    val date = LocalDate.parse("2000-08-31")
    pageHelper.enterDate(element, date)
    then(element).should().click()
    then(element).should().clear()
    then(element).should().sendKeys("31/08/2000")
  }

  @Test
  fun `given a null date when enterDate is called then element is cleared`() {
    val date: LocalDate? = null
    pageHelper.enterDate(element, date)
    then(element).should().click()
    then(element).should().clear()
    then(element).should(never()).sendKeys(any())
  }

  @Test
  fun `given a number when enterInteger is called then number is sent to element`() {
    val number = Random.nextInt()
    pageHelper.enterInteger(element, number)
    then(element).should().clear()
    then(element).should().sendKeys(number.toString())
  }

  @Test
  fun `given a null number when enterInteger is called then element is cleared`() {
    val number: Int? = null
    pageHelper.enterInteger(element, number)
    then(element).should().clear()
    then(element).should(never()).sendKeys(any())
  }

  @Test
  fun `given some text when enterTextIfNotBlank is called then text is sent`() {
    val text = randomString("text")
    pageHelper.enterTextIfNotBlank(element, text)
    then(element).should().sendKeys(text)
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["  ", "\t", "\n"])
  fun `given text is null or blank when enterTextIfNotBlank is called then text is not sent`(text: String?) {
    pageHelper.enterTextIfNotBlank(element, text)
    then(element).should(never()).sendKeys(any())
  }

  @Test
  fun `given a date input with a date when readDate is called then date is returned`() {
    given(element.getValue()).willReturn("31/12/2020")
    val result = pageHelper.readDate(element)
    assertEquals(LocalDate.parse("2020-12-31"), result)
  }

  @Test
  fun `given a date input with a blank date when readDate is called then exception is thrown`() {
    given(element.getValue()).willReturn("")
    val ex = assertThrows<AutomationException> {
      pageHelper.readDate(element)
    }
    assertEquals("Expected valid date in element but value was ''", ex.message)
  }

  @Test
  fun `given a date input with a date when readDateOrNull is called then date is returned`() {
    given(element.getValue()).willReturn("30/06/2015")
    val result = pageHelper.readDateOrNull(element)
    assertEquals(LocalDate.parse("2015-06-30"), result)
  }

  @Test
  fun `given a date input with a blank date when readDateOrNull is called then null is returned`() {
    given(element.getValue()).willReturn("")
    val result = pageHelper.readDateOrNull(element)
    assertNull(result)
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
