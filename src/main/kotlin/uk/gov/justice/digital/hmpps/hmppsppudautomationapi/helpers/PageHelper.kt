package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException

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
