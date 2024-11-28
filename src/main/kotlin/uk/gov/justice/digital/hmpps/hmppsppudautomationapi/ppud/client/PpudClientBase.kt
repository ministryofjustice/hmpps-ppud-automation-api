package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.PpudErrorException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage

internal abstract class PpudClientBase(
  protected val ppudUrl: String,
  private val ppudUsername: String,
  private val ppudPassword: String,
  private val ppudAdminUsername: String,
  private val ppudAdminPassword: String,
  protected val driver: WebDriver,
  private val errorPage: ErrorPage,
  private val loginPage: LoginPage,
  protected val searchPage: SearchPage,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val relativeLogoutUrl = "/logout.aspx"

  protected suspend fun <T> performLoggedInOperation(
    asAdmin: Boolean = false,
    disableRetry: Boolean = false,
    operation: suspend () -> T,
  ): T {
    // Modal dialogs aren't scrollable, and their contents can be inaccessible by Selenium if outside of the viewport. Maximising the window to maximise the viewport.
    driver.manage()?.window()?.maximize()

    if (asAdmin) {
      loginAsAdmin()
    } else {
      login()
    }

    val result = try {
      if (disableRetry) {
        operation.invoke()
      } else {
        operation.invokeWithRetry()
      }
    } catch (ex: WebDriverException) {
      throw wrapWebDriverException(ex)
    } finally {
      logout()
    }

    return result
  }

  fun quit() {
    driver.quit()
  }

  private fun loginAsAdmin() {
    login(ppudAdminUsername, ppudAdminPassword)
  }

  private fun login(username: String = ppudUsername, password: String = ppudPassword) {
    driver.navigate().to("${ppudUrl}${loginPage.urlPath}")
    loginPage.verifyOn()
    loginPage.login(username, password)
    loginPage.throwIfInvalid()
    searchPage.verifyOn()
  }

  private fun logout() {
    try {
      driver.navigate().to("$ppudUrl$relativeLogoutUrl")
    } catch (ex: Exception) {
      log.error("Error attempting to log out of PPUD", ex)
    }
  }

  private suspend fun <T> (suspend () -> T).invokeWithRetry(): T {
    return try {
      this()
    } catch (ex: WebDriverException) {
      val exceptionToLog = wrapWebDriverException(ex)
      log.error("Exception occurred but operation will be retried.", exceptionToLog)
      this()
    }
  }

  private fun wrapWebDriverException(ex: WebDriverException): AutomationException {
    return if (errorPage.isShown()) {
      PpudErrorException(
        "PPUD has displayed an error. Details are: '${errorPage.extractErrorDetails()}'",
        ex,
      )
    } else {
      AutomationException(
        "Exception occurred when performing PPUD operation. Current URL is '${driver.currentUrl}'",
        ex,
      )
    }
  }
}
