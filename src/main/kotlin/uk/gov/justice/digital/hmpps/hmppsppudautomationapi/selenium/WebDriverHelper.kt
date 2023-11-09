package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.WebElement

fun WebElement?.getValue(): String {
  return this?.getAttribute("value")?.trim() ?: ""
}
