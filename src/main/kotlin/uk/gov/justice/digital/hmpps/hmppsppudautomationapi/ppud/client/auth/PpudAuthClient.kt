package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.auth.PpudAuthConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.PpudOperationClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import java.util.function.Supplier

@Service
internal class PpudAuthClient {

  @Autowired
  private lateinit var ppudAuthConfig: PpudAuthConfig

  @Autowired
  private lateinit var ppudClientConfig: PpudClientConfig

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var loginPage: LoginPage

  @Autowired
  private lateinit var searchPage: SearchPage

  @Autowired
  private lateinit var operationClient: PpudOperationClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val relativeLogoutUrl = "/logout.aspx"

  suspend fun <T> performLoggedInOperation(
    asAdmin: Boolean = false,
    retryOnFailure: Boolean,
    operation: Supplier<T>,
  ): T {
    try {
      if (asAdmin) {
        logInAsAdmin()
      } else {
        logInAsUser()
      }
      return operationClient.invoke(retryOnFailure, operation)
    } finally {
      logOut()
    }
  }

  private fun logInAsAdmin() {
    logIn(ppudAuthConfig.adminUsername, ppudAuthConfig.adminPassword)
  }

  private fun logInAsUser() {
    logIn(ppudAuthConfig.username, ppudAuthConfig.password)
  }

  private fun logIn(username: String, password: String) {
    driver.navigate().to("${ppudClientConfig.url}${loginPage.urlPath}")
    loginPage.verifyOn()
    loginPage.login(username, password)
    loginPage.throwIfInvalid()
    searchPage.verifyOn()
  }

  private fun logOut() {
    try {
      driver.navigate().to("${ppudClientConfig.url}$relativeLogoutUrl")
    } catch (ex: Exception) {
      log.error("Error attempting to log out of PPUD", ex)
    }
  }

}