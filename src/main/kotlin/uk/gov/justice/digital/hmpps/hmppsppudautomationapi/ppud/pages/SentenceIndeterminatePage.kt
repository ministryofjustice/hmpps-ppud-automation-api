package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.extractId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
internal class SentenceIndeterminatePage(driver: WebDriver, private val dateFormatter: DateTimeFormatter) :
  SentencePage(driver) {

  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  override fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence {
    return Sentence(
      id = extractId(driver, "indeterminate sentence page"),
      dateOfSentence = LocalDate.parse(dateOfSentenceInput.getValue(), dateFormatter),
      custodyType = Select(custodyTypeDropdown).firstSelectedOption.text,
      // TODO
      licenceExpiryDate = null,
      mappaLevel = "",
      // Do releases last because it navigates away
      releases = releaseExtractor(determineReleaseLinks(includeEmptyReleases)),
      // TODO
      sentencingCourt = "",
    )
  }
}
