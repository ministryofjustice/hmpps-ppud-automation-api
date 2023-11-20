package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

fun WebElement?.getValue(): String {
  return this?.getAttribute("value")?.trim() ?: ""
}

fun WebElement?.enterTextIfNotBlank(text: String?) {
  if (!text.isNullOrBlank()) {
    this?.sendKeys(text)
  }
}

fun selectDropdownOptionIfNotBlank(dropdown: WebElement?, option: String?) {
  if (option?.isNotBlank() == true && dropdown != null) {
    Select(dropdown).selectByVisibleText(option)
  }
}
