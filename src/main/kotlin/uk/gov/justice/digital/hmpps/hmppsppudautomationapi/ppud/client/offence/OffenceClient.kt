package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offence

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffencePage

@Service
internal class OffenceClient {

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var ppudClientConfig: PpudClientConfig

  @Autowired
  private lateinit var offencePage: OffencePage

  fun getOffence(offenceUrl: String): Offence {
    driver.navigate().to("${ppudClientConfig.url}$offenceUrl")
    return offencePage.extractOffenceDetails()
  }
}