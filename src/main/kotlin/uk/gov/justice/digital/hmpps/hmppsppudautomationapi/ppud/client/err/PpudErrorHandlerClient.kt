package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.err

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.PpudErrorException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage

@Service
class PpudErrorHandlerClient {

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var errorPage: ErrorPage

  fun handleException(ex: WebDriverException): AutomationException = if (errorPage.isShown()) {
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
