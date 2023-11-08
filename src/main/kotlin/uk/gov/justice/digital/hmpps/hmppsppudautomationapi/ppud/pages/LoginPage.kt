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
internal class LoginPage(private val driver: WebDriver) {

  val urlFragment = "/Secure/MJSLogin.aspx"

  private val title = "PPUD - Login"

  @FindBy(id = "Login1_UserName")
  private val userNameInput: WebElement? = null

  @FindBy(id = "Login1_Password")
  private val passwordInput: WebElement? = null

  @FindBy(id = "Login1_LoginButton")
  private val loginButton: WebElement? = null

  init {
    PageFactory.initElements(driver, this)
  }

  fun login(userName: String, password: String) {
    userNameInput?.sendKeys(userName)
    passwordInput?.sendKeys(password)
    loginButton?.click()
  }

  fun verifyOn(): LoginPage {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
    return this
  }
}
