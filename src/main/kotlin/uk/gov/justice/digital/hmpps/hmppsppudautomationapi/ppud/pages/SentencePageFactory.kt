package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.springframework.stereotype.Component

@Component
internal class SentencePageFactory(
  val driver: WebDriver,
  val sentenceDeterminatePage: SentenceDeterminatePage,
  val sentenceIndeterminatePage: SentenceIndeterminatePage,
) {

  fun sentencePage(): SentencePage {
    return if (driver.currentUrl.contains("SentenceIndeterminateDetails.aspx", ignoreCase = true)) {
      sentenceIndeterminatePage
    } else {
      sentenceDeterminatePage
    }
  }
}
