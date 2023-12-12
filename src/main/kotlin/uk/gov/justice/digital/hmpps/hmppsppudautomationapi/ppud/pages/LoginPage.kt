package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.time.Duration

@Component
@RequestScope
internal class LoginPage(private val driver: WebDriver) {

  val urlPath = "/Secure/MJSLogin.aspx"

  private val title = "PPUD - Login"

  @FindBy(id = "Login1_UserName")
  private lateinit var userNameInput: WebElement

  @FindBy(id = "Login1_Password")
  private lateinit var passwordInput: WebElement

  @FindBy(id = "Login1_LoginButton")
  private lateinit var loginButton: WebElement

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    PageFactory.initElements(driver, this)
  }

  fun verifyOn() {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
  }

  fun login(userName: String, password: String) {
    log.info("***** DEBUG **** Logging in as '$userName' with password '$password'")
    userNameInput.sendKeys(userName)
    passwordInput.sendKeys(password)
    loginButton.click()
  }
}
