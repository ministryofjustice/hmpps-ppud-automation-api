package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException

fun WebElement.getValue(): String {
  return this.getAttribute("value")?.trim() ?: ""
}

fun WebElement.enterTextIfNotBlank(text: String?) {
  if (!text.isNullOrBlank()) {
    this.sendKeys(text)
  }
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
