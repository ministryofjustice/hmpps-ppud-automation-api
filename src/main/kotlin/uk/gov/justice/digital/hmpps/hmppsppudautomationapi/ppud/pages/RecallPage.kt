package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.enterTextIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank

@Component
@RequestScope
class RecallPage(
  private val driver: WebDriver,
  @Value("\${ppud.recall.revocationIssuedByOwner}") private val revocationIssuedByOwner: String,
  @Value("\${ppud.recall.recallType}") private val recallType: String,
) {

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private val saveButton: WebElement? = null

  @FindBy(id = "cntDetails_ddliRECALL_TYPE")
  private val recallTypeDropdown: WebElement? = null

  @FindBy(id = "cntDetails_ddliPROBATION_AREA")
  private val probationAreaDropdown: WebElement? = null

  @FindBy(id = "cntDetails_chkUAL_FLAG")
  private val ualCheckbox: WebElement? = null

  @FindBy(id = "cntDetails_ddliMAPPA_LEVEL")
  private val mappaLevelDropdown: WebElement? = null

  @FindBy(id = "igtxtcntDetails_dteUAL_CHECK")
  private val nextUalCheckInput: WebElement? = null

  @FindBy(id = "igtxtcntDetails_dtePB_DECISION_AFTER_BREACH_ACTUAL")
  private val decisionFollowingBreachDateInput: WebElement? = null

  @FindBy(id = "igtxtcntDetails_dteREPORT_RECD_BY_UNIT_ACTUAL")
  private val reportReceivedDateInput: WebElement? = null

  @FindBy(id = "igtxtcntDetails_dteRECOMMEND_TO_EO_ACTUAL")
  private val recommendedToDateInput: WebElement? = null

  @FindBy(id = "cntDetails_aceiRECOMMEND_TO_EO_CWORKER_AutoCompleteTextBox")
  private val recommendedToOwnerInput: WebElement? = null

  @FindBy(id = "cntDetails_aceiRECOMMEND_TO_EO_CWORKER_AutoSelect")
  private val recommendedToOwnerDropdown: WebElement? = null

  @FindBy(id = "cntDetails_aceiREVOCATION_ISSUED_BY_AutoCompleteTextBox")
  private val revocationIssuedByOwnerInput: WebElement? = null

  @FindBy(id = "cntDetails_aceiREVOCATION_ISSUED_BY_AutoSelect")
  private val revocationIssuedByOwnerDropdown: WebElement? = null

  @FindBy(id = "cntDetails_ddliPOLICE_FORCE")
  private val policeForceDropdown: WebElement? = null

  @FindBy(id = "cntDetails_ddliRTC_NOTIF_METHOD")
  private val returnToCustodyNotificationMethodDropdown: WebElement? = null

  @FindBy(id = "cntDetails_ddliMAND_DOCS_RECEIVED")
  private val mandatoryDocumentsReceivedDropdown: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_PART_A")
  private val missingPartACheckbox: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_OASYS")
  private val missingOaSysCheckbox: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_PRE_SENTENCE_REP")
  private val missingPreSentenceReportCheckbox: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_PREV_CONV")
  private val missingPreviousConvictionsCheckbox: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_LICENCE")
  private val missingLicenceCheckbox: WebElement? = null

  @FindBy(id = "cntDetails_chkMAND_DOC_CHARGE_SHEET")
  private val missingChargeSheetCheckbox: WebElement? = null

  private val addMinuteButton: WebElement?
    get() = driver.findElements(By.id("cntDetails_PageFooter1_Minutes1_btnReplyTop")).firstOrNull()

  private val minuteEditor: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_MinutesTextRich_tw"))

  private val saveMinuteButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_btnSave"))

  private val uploadDocumentButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_cmdUploadDoc"))

  private val deliveryActualInput: WebElement
    get() = driver.findElement(By.id("igtxtcntDetails_PageFooter1_docEdit_dteDELIVERY_ACTUAL"))

  private val documentTitleInput: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_docEdit_txtDOCUMENT_TITLE"))

  private val replyActualInput: WebElement
    get() = driver.findElement(By.id("igtxtcntDetails_PageFooter1_docEdit_dteREPLY_ACTUAL"))

  private val documentTypeDropdown: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_docEdit_ddliDOCUMENT_TYPE"))

  private val chooseFileInput: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_docEdit_fUpDocuments"))

  private val saveAndAddMoreDocumentsButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_docEdit_cmdSaveAndAdd"))

  private val closeDocumentUploadButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_docEdit_cmdCancel"))

  private val documentUploadStatusTable: WebElement
    get() = driver.findElement(By.id("UploadStatusData"))

  private val documentUploadStatuses: List<WebElement>
    get() = documentUploadStatusTable.findElements(By.xpath(".//td[starts-with(@id, 'upload_1')]"))

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  suspend fun createRecall(createRecallRequest: CreateRecallRequest) {
    // Complete these first as they trigger additional processing
    // Autocomplete box doesn't work with brackets
    val recommendedToOwnerSearchable = createRecallRequest.recommendedToOwner.takeWhile { (it == '(').not() }
    recommendedToOwnerInput?.click()
    recommendedToOwnerInput.enterTextIfNotBlank(recommendedToOwnerSearchable)
    val revocationIssuedByOwnerSearchable = revocationIssuedByOwner.takeWhile { (it == '(').not() }
    revocationIssuedByOwnerInput?.click()
    revocationIssuedByOwnerInput.enterTextIfNotBlank(revocationIssuedByOwnerSearchable)

    // Complete standalone fields
    selectDropdownOptionIfNotBlank(recallTypeDropdown, recallType)
  }

  suspend fun addMinute(createRecallRequest: CreateRecallRequest) {
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed Creating Recall.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  private fun generateMinuteText(createRecallRequest: CreateRecallRequest): String {
    return ""
  }

  fun extractRecallDetails(): Recall {
    return Recall("")
  }
}
