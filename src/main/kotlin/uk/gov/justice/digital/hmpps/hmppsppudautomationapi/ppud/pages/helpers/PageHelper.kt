package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers

import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class PageHelper(val dateFormatter: DateTimeFormatter) {

  companion object {
    fun WebElement.getValue(): String {
      return this.getAttribute("value")?.trim() ?: ""
    }
  }

  fun dismissConfirmDeleteAlert(driver: WebDriver) {
    val alert = driver.switchTo().alert()
    if (alert.text.contains("This will delete the whole record", ignoreCase = true)) {
      alert.accept()
    } else {
      throw AutomationException("Alert shown with the text '${alert.text}")
    }
  }

  fun dismissCheckCapitalisationAlert(driver: WebDriver, nextElement: WebElement) {
    try {
      nextElement.click()
      val alert = driver.switchTo().alert()
      if (alert.text.contains("check that the capitalisation is correct")) {
        alert.accept()
      } else {
        throw AutomationException("Alert shown with the text '${alert.text}")
      }
    } catch (ex: NoAlertPresentException) {
      // No alert so we can proceed
    }
  }

  fun enterDate(dateInput: WebElement, date: LocalDate?) {
    dateInput.click()
    dateInput.clear()
    if (date != null) {
      dateInput.sendKeys(date.format(dateFormatter))
    }
  }

  fun enterInteger(input: WebElement, number: Int?) {
    input.clear()
    if (number != null) {
      input.sendKeys(number.toString())
    }
  }

  fun enterText(input: WebElement, text: String) {
    input.clear()
    input.sendKeys(text)
  }

  fun enterTextIfNotBlank(input: WebElement, text: String?) {
    if (!text.isNullOrBlank()) {
      enterText(input, text)
    }
  }

  fun extractId(driver: WebDriver, pageDescription: String): String {
    val url = driver.currentUrl
    val idMatch = Regex(".+?data=(.+)").find(url)
      ?: throw AutomationException("Expected the $pageDescription but URL was '$url'")
    val (id) = idMatch.destructured
    return id
  }

  fun readDate(input: WebElement): LocalDate {
    return readDateOrNull(input)
      ?: throw AutomationException("Expected valid date in element but value was '${input.getValue()}'")
  }

  fun readDateOrNull(input: WebElement): LocalDate? {
    val inputValue = input.getValue()
    return if (inputValue.isNotBlank()) {
      LocalDate.parse(inputValue, dateFormatter)
    } else {
      null
    }
  }

  fun readIntegerOrDefault(input: WebElement, default: Int): Int {
    val inputValue = input.getValue()
    return inputValue.toIntOrNull() ?: default
  }

  fun readSelectedOption(dropdown: WebElement): String {
    return Select(dropdown).firstSelectedOption.text
  }

  fun selectDropdownOptionIfNotBlank(dropdown: WebElement, option: String?, description: String) {
    if (option?.isNotBlank() == true) {
      val select = Select(dropdown)
      try {
        select.selectByVisibleText(option)
      } catch (ex: org.openqa.selenium.NoSuchElementException) {
        throw AutomationException("Cannot locate $description option with text '$option'")
      }
    }
  }

  fun selectCheckboxValue(checkbox: WebElement, value: Boolean) {
    if (checkbox.isSelected != value) {
      checkbox.click()
    }
  }

  fun waitForDropdownPopulation(driver: WebDriver, dropdown: WebElement) {
    val dropdownAsSelect = Select(dropdown)
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until { dropdownAsSelect.options.any() }
  }
}
