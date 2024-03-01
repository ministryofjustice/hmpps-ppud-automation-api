package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.slf4j.LoggerFactory
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

  protected suspend fun <T> performLoggedInOperation(asAdmin: Boolean = false, operation: suspend () -> T): T {
    if (asAdmin) {
      loginAsAdmin()
    } else {
      login()
    }
    val result = try {
      operation()
    } catch (ex: Exception) {
      if (ex is WebDriverException && errorPage.isShown()) {
        throw PpudErrorException("PPUD has displayed an error. Details are: '${errorPage.extractErrorDetails()}'", ex)
      } else {
        throw ex
      }
    } finally {
      logout()
    }
    return result
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
}
