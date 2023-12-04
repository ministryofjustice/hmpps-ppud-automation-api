package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.time.Duration

@Component
@RequestScope
internal class AdminPage(private val driver: WebDriver) {

  val urlPath = "/Admin/AdminFunctions.aspx"

  private val title = "Administration Menu"

  @FindBy(linkText = "Lookup Edit")
  private lateinit var editLookupsLink: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun verifyOn() {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
  }

  fun goToEditLookups() {
    editLookupsLink.click()
  }
}
