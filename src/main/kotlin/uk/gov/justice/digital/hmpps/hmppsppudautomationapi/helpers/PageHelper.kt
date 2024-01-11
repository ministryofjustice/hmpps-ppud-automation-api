package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException

fun dismissConfirmDeleteAlert(driver: WebDriver) {
  val alert = driver.switchTo().alert()
  if (alert.text.contains("This will delete the whole record", ignoreCase = true)) {
    alert.accept()
  } else {
    throw AutomationException("Alert shown with the text '${alert.text}")
  }
}
