package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.SentenceComparator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue

@Component
internal class SentenceIndeterminatePage(
  driver: WebDriver,
  pageHelper: PageHelper,
  navigationTreeViewComponent: NavigationTreeViewComponent,
  sentenceComparator: SentenceComparator,
) : BaseSentencePage(driver, pageHelper, navigationTreeViewComponent, sentenceComparator) {
  // Initialize
  init {
    PageFactory.initElements(driver, this)
  }

  // Implement abstract
  override val pageDescription: String
    get() = "indeterminate sentence page"

  override fun selectCustodyType(custodyType: String) {
    pageHelper.selectDropdownOptionIfNotBlank(custodyTypeDropdown, custodyType, "custody type")
  }

  override fun createSentence(request: CreateOrUpdateSentenceRequest) {
    TODO("Indeterminate sentences not yet supported")
  }

  override fun updateSentence(request: CreateOrUpdateSentenceRequest) {
    with(pageHelper) {
      enterDate(dateOfSentenceInput, request.dateOfSentence)
      enterText(sentencingCourtInput, request.sentencingCourt)
    }

    saveButton.click()
  }

  override fun extractCreatedSentenceDetails(): CreatedSentence {
    TODO("Indeterminate sentences not yet supported")
  }

  override fun extractSentenceDetails(
    offenceExtractor: (String) -> Offence,
  ): Sentence {
    val offenceLink = determineOffenceLink()
    return with(pageHelper) {
      val sentenceId = extractId(pageDescription)
      Sentence(
        id = sentenceId,
        custodyType = Select(custodyTypeDropdown).firstSelectedOption.text,
        dateOfSentence = readDate(dateOfSentenceInput),
        releaseDate = navigationTreeViewComponent.latestReleaseDate(sentenceId),
        tariffExpiryDate = readDateFromTextOrNull(tariffExpiryDate),
        sentenceLength = SentenceLength(
          readTextAsIntegerOrDefault(fullPunishmentYearsInput, 0),
          readTextAsIntegerOrDefault(fullPunishmentMonthsInput, 0),
          readTextAsIntegerOrDefault(fullPunishmentDaysInput, 0),
        ),
        sentencingCourt = sentencingCourtInput.getValue(),
        // Do offence last because it navigates away
        offence = offenceExtractor(offenceLink),
      )
    }
  }

  // Page Elements
  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "cntDetails_lbliTARIFF_FP_YRS")
  private lateinit var fullPunishmentYearsInput: WebElement

  @FindBy(id = "cntDetails_lbliTARIFF_FP_MNTHS")
  private lateinit var fullPunishmentMonthsInput: WebElement

  @FindBy(id = "cntDetails_lbliTARIFF_FP_DAYS")
  private lateinit var fullPunishmentDaysInput: WebElement

  @FindBy(id = "cntDetails_lbldTARIFF_EXPIRY_DATE")
  private lateinit var tariffExpiryDate: WebElement

  @FindBy(id = "cntDetails_txtSENTENCING_COURT")
  private lateinit var sentencingCourtInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement
}
