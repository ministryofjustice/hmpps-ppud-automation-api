package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.enterTextIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectCheckboxValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@RequestScope
class RecallPage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
  private val dateTimeFormatter: DateTimeFormatter,
  @Value("\${ppud.recall.revocationIssuedByOwner}") private val revocationIssuedByOwner: String,
  @Value("\${ppud.recall.recallType}") private val recallType: String,
  @Value("\${ppud.recall.returnToCustodyNotificationMethod}") private val returnToCustodyNotificationMethod: String,
  @Value("\${ppud.recall.nextUalCheckMonths}") private val nextUalCheckMonths: Long,
) {

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_ddliRECALL_TYPE")
  private lateinit var recallTypeDropdown: WebElement

  @FindBy(id = "cntDetails_ddliPROBATION_AREA")
  private lateinit var probationAreaDropdown: WebElement

  @FindBy(id = "cntDetails_chkUAL_FLAG")
  private lateinit var ualCheckbox: WebElement

  @FindBy(id = "cntDetails_ddliMAPPA_LEVEL")
  private lateinit var mappaLevelDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteUAL_CHECK")
  private lateinit var nextUalCheckInput: WebElement

  @FindBy(id = "igtxtcntDetails_dtePB_DECISION_AFTER_BREACH_ACTUAL")
  private lateinit var decisionFollowingBreachDateInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteREPORT_RECD_BY_UNIT_ACTUAL")
  private lateinit var reportReceivedDateInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteRECOMMEND_TO_EO_ACTUAL")
  private lateinit var recommendedToDateInput: WebElement

  @FindBy(id = "cntDetails_aceiRECOMMEND_TO_EO_CWORKER_AutoCompleteTextBox")
  private lateinit var recommendedToOwnerInput: WebElement

  @FindBy(id = "cntDetails_aceiRECOMMEND_TO_EO_CWORKER_AutoSelect")
  private lateinit var recommendedToOwnerDropdown: WebElement

  @FindBy(id = "cntDetails_aceiREVOCATION_ISSUED_BY_AutoCompleteTextBox")
  private lateinit var revocationIssuedByOwnerInput: WebElement

  @FindBy(id = "cntDetails_aceiREVOCATION_ISSUED_BY_AutoSelect")
  private lateinit var revocationIssuedByOwnerDropdown: WebElement

  @FindBy(id = "cntDetails_ddliPOLICE_FORCE")
  private lateinit var policeForceDropdown: WebElement

  @FindBy(id = "cntDetails_ddliRTC_NOTIF_METHOD")
  private lateinit var returnToCustodyNotificationMethodDropdown: WebElement

  @FindBy(id = "cntDetails_ddliMAND_DOCS_RECEIVED")
  private lateinit var mandatoryDocumentsReceivedDropdown: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_PART_A")
  private lateinit var missingPartACheckbox: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_OASYS")
  private lateinit var missingOaSysCheckbox: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_PRE_SENTENCE_REP")
  private lateinit var missingPreSentenceReportCheckbox: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_PREV_CONV")
  private lateinit var missingPreviousConvictionsCheckbox: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_LICENCE")
  private lateinit var missingLicenceCheckbox: WebElement

  @FindBy(id = "cntDetails_chkMAND_DOC_CHARGE_SHEET")
  private lateinit var missingChargeSheetCheckbox: WebElement

  private val addMinuteButton: WebElement?
    get() = driver.findElements(By.id("cntDetails_PageFooter1_Minutes1_btnReplyTop")).firstOrNull()

  private val minuteEditor: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_MinutesTextRich_tw"))

  private val saveMinuteButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_btnSave"))

  private val deliveryActualInput: WebElement
    get() = driver.findElement(By.id("igtxtcntDetails_PageFooter1_docEdit_dteDELIVERY_ACTUAL"))

  private val replyActualInput: WebElement
    get() = driver.findElement(By.id("igtxtcntDetails_PageFooter1_docEdit_dteREPLY_ACTUAL"))

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  suspend fun createRecall(createRecallRequest: CreateRecallRequest) {
    // Complete these first as they trigger additional processing
    // Autocomplete box doesn't work with brackets
    val recommendedToOwnerSearchable = createRecallRequest.recommendedToOwner.takeWhile { (it == '(').not() }
    recommendedToOwnerInput.click()
    recommendedToOwnerInput.enterTextIfNotBlank(recommendedToOwnerSearchable)
    val revocationIssuedByOwnerSearchable = revocationIssuedByOwner.takeWhile { (it == '(').not() }
    revocationIssuedByOwnerInput.click()
    revocationIssuedByOwnerInput.enterTextIfNotBlank(revocationIssuedByOwnerSearchable)

    // Complete standalone fields
    selectDropdownOptionIfNotBlank(recallTypeDropdown, recallType)
    selectDropdownOptionIfNotBlank(probationAreaDropdown, createRecallRequest.probationArea)
    selectCheckboxValue(ualCheckbox, createRecallRequest.isInCustody)
    if (createRecallRequest.isInCustody) {
      selectDropdownOptionIfNotBlank(returnToCustodyNotificationMethodDropdown, returnToCustodyNotificationMethod)
    } else {
      val nextUalCheckDate = LocalDateTime.now().plusMonths(nextUalCheckMonths).format(dateFormatter)
      nextUalCheckInput.enterTextIfNotBlank(nextUalCheckDate)
    }
    decisionFollowingBreachDateInput.enterTextIfNotBlank(createRecallRequest.decisionDateTime.format(dateTimeFormatter))
    reportReceivedDateInput.enterTextIfNotBlank(createRecallRequest.receivedDateTime.format(dateTimeFormatter))
    recommendedToDateInput.enterTextIfNotBlank(LocalDateTime.now().format(dateTimeFormatter))
    selectDropdownOptionIfNotBlank(policeForceDropdown, createRecallRequest.policeForce)
    selectDropdownOptionIfNotBlank(mandatoryDocumentsReceivedDropdown, "No")
    checkAllMissingMandatoryDocuments()

    // Complete fields that have been updated/refreshed.
    waitForDropdownPopulation(recommendedToOwnerDropdown)
    selectDropdownOptionIfNotBlank(recommendedToOwnerDropdown, createRecallRequest.recommendedToOwner)
    waitForDropdownPopulation(revocationIssuedByOwnerDropdown)
    selectDropdownOptionIfNotBlank(revocationIssuedByOwnerDropdown, revocationIssuedByOwner)

    saveButton.click()
  }

  suspend fun addMinute(createRecallRequest: CreateRecallRequest) {
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed Creating Recall.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  fun extractRecallDetails(): Recall {
    return Recall("")
  }

  private fun waitForDropdownPopulation(dropdown: WebElement) {
    val dropdownAsSelect = Select(dropdown)
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until { dropdownAsSelect.options.any() }
  }

  private fun checkAllMissingMandatoryDocuments() {
    selectCheckboxValue(missingPartACheckbox, true)
    selectCheckboxValue(missingOaSysCheckbox, true)
    selectCheckboxValue(missingPreSentenceReportCheckbox, true)
    selectCheckboxValue(missingPreviousConvictionsCheckbox, true)
    selectCheckboxValue(missingLicenceCheckbox, true)
    selectCheckboxValue(missingChargeSheetCheckbox, true)
  }
}
