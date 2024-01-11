package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
internal class SentenceDeterminatePage(driver: WebDriver, private val dateFormatter: DateTimeFormatter) :
  SentencePage(driver) {

  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "cntDetails_ddliMAPPA_Level")
  private lateinit var mappaLevelDropdown: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  override fun extractSentenceDetails(releaseExtractor: (List<String>) -> List<Release>): Sentence {
    return Sentence(
      dateOfSentence = LocalDate.parse(dateOfSentenceInput.getValue(), dateFormatter),
      custodyType = Select(custodyTypeDropdown).firstSelectedOption.text,
      mappaLevel = Select(mappaLevelDropdown).firstSelectedOption.text,
      // Do releases last because it navigates away
      releases = releaseExtractor(determineReleaseLinks()),
    )
  }
}
