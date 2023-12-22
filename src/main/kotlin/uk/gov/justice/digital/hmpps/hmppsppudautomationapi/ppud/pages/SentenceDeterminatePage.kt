package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
internal class SentenceDeterminatePage(driver: WebDriver, private val dateFormatter: DateTimeFormatter) :
  SentencePage {

  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "cntDetails_ddliMAPPA_Level")
  private lateinit var mappaLevelDropdown: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  override fun extractSentenceDetails(): Sentence {
    return Sentence(
      LocalDate.parse(dateOfSentenceInput.getValue(), dateFormatter),
      Select(custodyTypeDropdown).firstSelectedOption.text,
      Select(mappaLevelDropdown).firstSelectedOption.text,
    )
  }
}
