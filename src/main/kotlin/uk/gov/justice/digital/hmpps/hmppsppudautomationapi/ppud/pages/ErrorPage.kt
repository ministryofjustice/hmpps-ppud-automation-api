package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper

@Component
internal class ErrorPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
) {

  private val body: WebElement?
    get() = driver.findElements(By.xpath("//body")).firstOrNull()

  fun isShown(): Boolean {
    return pageHelper.isCustomErrorUrl() ||
      body?.text?.startsWith("Server Error in") == true
  }

  fun extractErrorDetails(): String {
    return if (pageHelper.isCustomErrorUrl()) {
      "An error has occurred"
    } else {
      driver.title
    }
  }
}
