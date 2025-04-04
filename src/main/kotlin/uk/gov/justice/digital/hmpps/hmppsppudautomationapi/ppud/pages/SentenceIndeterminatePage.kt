package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

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
) : SentencePage(driver, pageHelper, navigationTreeViewComponent, sentenceComparator) {

  @FindBy(id = "cntDetails_ddliCUSTODY_TYPE")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOS")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "cntDetails_txtSENTENCING_COURT")
  private lateinit var sentencingCourtInput: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  override val pageDescription: String
    get() = "indeterminate sentence page"

  override fun selectCustodyType(custodyType: String) {
    pageHelper.selectDropdownOptionIfNotBlank(custodyTypeDropdown, custodyType, "custody type")
  }

  override fun createSentence(request: CreateOrUpdateSentenceRequest) {
    TODO("Indeterminate sentences not yet supported")
  }

  override fun updateSentence(request: CreateOrUpdateSentenceRequest) {
    TODO("Indeterminate sentences not yet supported")
  }

  override fun extractCreatedSentenceDetails(): CreatedSentence {
    TODO("Indeterminate sentences not yet supported")
  }

  override fun extractSentenceDetails(
    offenceExtractor: (String) -> Offence,
  ): Sentence {
    val offenceLink = determineOffenceLink()
    return Sentence(
      id = pageHelper.extractId(pageDescription),
      custodyType = Select(custodyTypeDropdown).firstSelectedOption.text,
      dateOfSentence = pageHelper.readDate(dateOfSentenceInput),
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      licenceExpiryDate = null,
      mappaLevel = "",
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = sentencingCourtInput.getValue(),
      // Do offence last because it navigates away
      offence = offenceExtractor(offenceLink),
    )
  }

  override fun throwIfInvalid() {
    TODO("Indeterminate sentences not yet supported")
  }
}
