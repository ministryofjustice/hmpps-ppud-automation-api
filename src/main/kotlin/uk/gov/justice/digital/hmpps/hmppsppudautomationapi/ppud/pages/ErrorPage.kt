package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper

@Component
@RequestScope
internal class ErrorPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
) {

  private val body: WebElement?
    get() = driver.findElements(By.xpath("//body")).firstOrNull()

  fun isShown(): Boolean {
    return pageHelper.isCustomErrorUrl(driver) ||
      body?.text?.startsWith("Server Error in") == true
  }

  fun extractErrorDetails(): String {
    return if (pageHelper.isCustomErrorUrl(driver)) {
      "An error has occurred"
    } else {
      driver.title
    }
  }
}
