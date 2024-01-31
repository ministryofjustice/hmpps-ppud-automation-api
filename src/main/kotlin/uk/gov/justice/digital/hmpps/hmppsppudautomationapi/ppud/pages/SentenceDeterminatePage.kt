package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.SentenceComparator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.EspPeriod
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue

@Component
internal class SentenceDeterminatePage(
  driver: WebDriver,
  pageHelper: PageHelper,
  navigationTreeViewComponent: NavigationTreeViewComponent,
  sentenceComparator: SentenceComparator,
  @Value("\${ppud.sentence.sentencedUnder}") private val sentencedUnder: String,
) : SentencePage(driver, pageHelper, navigationTreeViewComponent, sentenceComparator) {

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

  @FindBy(id = "igtxtcntDetails_dteLICENCE_START")
  private lateinit var releaseDateInput: WebElement

  @FindBy(id = "cntDetails_ddliSENTENCED_UNDER")
  private lateinit var sentencedUnderDropdown: WebElement

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
    pageHelper.selectDropdownOptionIfNotBlank(custodyTypeDropdown, custodyType, "custody type")
  }

  override fun createSentence(request: CreateOrUpdateSentenceRequest) {
    updateSentence(request)
  }

  override fun updateSentence(request: CreateOrUpdateSentenceRequest) {
    with(pageHelper) {
      enterDate(dateOfSentenceInput, request.dateOfSentence)
      enterInteger(espCustodialPeriodYearsInput, request.espCustodialPeriod?.years)
      enterInteger(espCustodialPeriodMonthsInput, request.espCustodialPeriod?.months)
      enterInteger(espExtendedPeriodYearsInput, request.espExtendedPeriod?.years)
      enterInteger(espExtendedPeriodMonthsInput, request.espExtendedPeriod?.months)
      enterDate(licenceExpiryDateInput, request.licenceExpiryDate)
      selectDropdownOptionIfNotBlank(mappaLevelDropdown, request.mappaLevel, "mappa level")
      enterDate(releaseDateInput, request.releaseDate)
      selectDropdownOptionIfNotBlank(sentencedUnderDropdown, sentencedUnder, "sentenced under")
      enterDate(sentenceExpiryDateInput, request.sentenceExpiryDate)
      enterText(sentencingCourtInput, request.sentencingCourt)
      enterInteger(sentenceLengthPartYearsInput, request.sentenceLength?.partYears)
      enterInteger(sentenceLengthPartMonthsInput, request.sentenceLength?.partMonths)
      enterInteger(sentenceLengthPartDaysInput, request.sentenceLength?.partDays)
    }

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
    return with(pageHelper) {
      Sentence(
        id = extractId(driver, pageDescription),
        custodyType = readSelectedOption(custodyTypeDropdown),
        dateOfSentence = readDate(dateOfSentenceInput),
        espCustodialPeriod = EspPeriod(
          years = readIntegerOrDefault(espCustodialPeriodYearsInput, 0),
          months = readIntegerOrDefault(espCustodialPeriodMonthsInput, 0),
        ),
        espExtendedPeriod = EspPeriod(
          years = readIntegerOrDefault(espExtendedPeriodYearsInput, 0),
          months = readIntegerOrDefault(espExtendedPeriodMonthsInput, 0),
        ),
        licenceExpiryDate = readDateOrNull(licenceExpiryDateInput),
        mappaLevel = readSelectedOption(mappaLevelDropdown),
        releaseDate = readDateOrNull(releaseDateInput),
        sentencedUnder = readSelectedOption(sentencedUnderDropdown),
        sentenceExpiryDate = readDateOrNull(sentenceExpiryDateInput),
        sentenceLength = SentenceLength(
          partYears = readIntegerOrDefault(sentenceLengthPartYearsInput, 0),
          partMonths = readIntegerOrDefault(sentenceLengthPartMonthsInput, 0),
          partDays = readIntegerOrDefault(sentenceLengthPartDaysInput, 0),
        ),
        sentencingCourt = sentencingCourtInput.getValue(),
        // Do offence and releases last because it navigates away
        offence = offenceExtractor(offenceLink),
        releases = releaseExtractor(releaseLinks),
      )
    }
  }

  override fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }
}
