package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import java.time.Duration

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

fun extractId(driver: WebDriver, pageDescription: String): String {
  val url = driver.currentUrl
  val idMatch = Regex(".+?data=(.+)").find(url)
    ?: throw AutomationException("Expected the $pageDescription but URL was '$url'")
  val (id) = idMatch.destructured
  return id
}

fun waitForDropdownPopulation(driver: WebDriver, dropdown: WebElement) {
  val dropdownAsSelect = Select(dropdown)
  WebDriverWait(driver, Duration.ofSeconds(2))
    .until { dropdownAsSelect.options.any() }
}
