package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank
import java.time.format.DateTimeFormatter

@Component
internal class SentenceDeterminatePage(
  driver: WebDriver,
  pageHelper: PageHelper,
  private val dateFormatter: DateTimeFormatter,
  navigationTreeViewComponent: NavigationTreeViewComponent,
) :
  SentencePage(driver, pageHelper, navigationTreeViewComponent) {

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtESP_CUSTODIAL_YRS")
  private lateinit var espCustodialPeriodYearsInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtESP_CUSTODIAL_MNTHS")
  private lateinit var espCustodialPeriodMonthsInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtESP_EXTENSION_YRS")
  private lateinit var espExtendedPeriodYearsInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtESP_EXTENSION_MNTHS")
  private lateinit var espExtendedPeriodMonthsInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteLICENCE_END")
  private lateinit var licenceExpiryDateInput: WebElement

  @FindBy(id = "cntDetails_ddliMAPPA_Level")
  private lateinit var mappaLevelDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteSED")
  private lateinit var sentenceExpiryDateInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtPART_YRS")
  private lateinit var sentenceLengthPartYearsInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtPART_MNTHS")
  private lateinit var sentenceLengthPartMonthsInput: WebElement

  @FindBy(id = "igtxtcntDetails_txtPART_DAYS")
  private lateinit var sentenceLengthPartDaysInput: WebElement

  @FindBy(id = "cntDetails_txtSENTENCING_COURT")
  private lateinit var sentencingCourtInput: WebElement

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  override val pageDescription: String
    get() = "determinate sentence page"

  override fun selectCustodyType(custodyType: String) {
    selectDropdownOptionIfNotBlank(custodyTypeDropdown, custodyType, "custody type")
  }

  override fun createSentence(request: CreateOrUpdateSentenceRequest) {
    dateOfSentenceInput.click()
    dateOfSentenceInput.sendKeys((request.dateOfSentence.format(dateFormatter)))
    selectDropdownOptionIfNotBlank(mappaLevelDropdown, request.mappaLevel, "mappa level")

    saveButton.click()
  }

  override fun extractCreatedSentenceDetails(): CreatedSentence {
    return CreatedSentence(
      id = pageHelper.extractId(driver, pageDescription),
    )
  }

  override fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    offenceExtractor: (String) -> Offence,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence {
    val releaseLinks = determineReleaseLinks(includeEmptyReleases)
    val offenceLink = determineOffenceLink()
    return Sentence(
      id = pageHelper.extractId(driver, pageDescription),
      custodyType = Select(custodyTypeDropdown).firstSelectedOption.text,
      dateOfSentence = pageHelper.readDate(dateOfSentenceInput),
      espCustodialPeriod = EspPeriod(
        years = pageHelper.readIntegerOrDefault(espCustodialPeriodYearsInput, 0),
        months = pageHelper.readIntegerOrDefault(espCustodialPeriodMonthsInput, 0),
      ),
      espExtendedPeriod = EspPeriod(
        years = pageHelper.readIntegerOrDefault(espExtendedPeriodYearsInput, 0),
        months = pageHelper.readIntegerOrDefault(espExtendedPeriodMonthsInput, 0),
      ),
      licenceExpiryDate = pageHelper.readDateOrNull(licenceExpiryDateInput),
      mappaLevel = Select(mappaLevelDropdown).firstSelectedOption.text,
      sentenceExpiryDate = pageHelper.readDateOrNull(sentenceExpiryDateInput),
      sentenceLength = SentenceLength(
        partYears = pageHelper.readIntegerOrDefault(sentenceLengthPartYearsInput, 0),
        partMonths = pageHelper.readIntegerOrDefault(sentenceLengthPartMonthsInput, 0),
        partDays = pageHelper.readIntegerOrDefault(sentenceLengthPartDaysInput, 0),
      ),
      sentencingCourt = sentencingCourtInput.getValue(),
      // Do offence and releases last because it navigates away
      offence = offenceExtractor(offenceLink),
      releases = releaseExtractor(releaseLinks),
    )
  }

  override fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }
}
