package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
internal class ApplicationControlPage(driver: WebDriver) {

  @FindBy(linkText = "Logout")
  private lateinit var logoutLink: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun logout() {
    logoutLink.click()
  }
}
