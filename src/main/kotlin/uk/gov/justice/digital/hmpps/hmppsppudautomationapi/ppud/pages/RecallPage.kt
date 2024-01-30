package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.ContentCreator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class RecallPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
  private val dateFormatter: DateTimeFormatter,
  private val dateTimeFormatter: DateTimeFormatter,
  private val contentCreator: ContentCreator,
  @Value("\${ppud.recall.revocationIssuedByOwner}") private val revocationIssuedByOwner: String,
  @Value("\${ppud.recall.recallType}") private val recallType: String,
  @Value("\${ppud.recall.returnToCustodyNotificationMethod}") private val returnToCustodyNotificationMethod: String,
  @Value("\${ppud.recall.nextUalCheckMonths}") private val nextUalCheckMonths: Long,
) {

  private val urlPathTemplate = "/Offender/Recall.aspx?data={id}"

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_PageFooter1_cmdDelete")
  private lateinit var deleteButton: WebElement

  @FindBy(id = "cntDetails_ddliRECALL_TYPE")
  private lateinit var recallTypeDropdown: WebElement

  @FindBy(id = "cntDetails_ddliPROBATION_AREA")
  private lateinit var probationAreaDropdown: WebElement

  @FindBy(id = "cntDetails_chkUAL_FLAG")
  private lateinit var ualCheckbox: WebElement

  @FindBy(id = "cntDetails_ddliMAPPA_LEVEL")
  private lateinit var mappaLevelDropdown: WebElement

  @FindBy(id = "cntDetails_aceiOWNING_TEAM_AutoCompleteTextBox")
  private lateinit var owningTeamInput: WebElement

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

  private val addMinuteButtonId = "cntDetails_PageFooter1_Minutes1_btnReplyTop"

  // We need to detect if the Add Minute button isn't available, rather than throw an exception
  private val addMinuteButton: WebElement?
    get() = driver.findElements(By.id(addMinuteButtonId)).firstOrNull()

  private val minuteEditor: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_MinutesTextRich_tw"))

  private val saveMinuteButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_btnSave"))

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  suspend fun createRecall(createRecallRequest: CreateRecallRequest) {
    // Complete these first as they trigger additional processing
    // Autocomplete box doesn't work with brackets
    val recommendedToOwnerSearchable = createRecallRequest.recommendedToOwner.takeWhile { (it == '(').not() }
    pageHelper.enterTextIfNotBlank(recommendedToOwnerInput, recommendedToOwnerSearchable)
    val revocationIssuedByOwnerSearchable = revocationIssuedByOwner.takeWhile { (it == '(').not() }
    pageHelper.enterTextIfNotBlank(revocationIssuedByOwnerInput, revocationIssuedByOwnerSearchable)

    // Complete standalone fields
    pageHelper.selectDropdownOptionIfNotBlank(recallTypeDropdown, recallType, "recall type")
    pageHelper.selectDropdownOptionIfNotBlank(
      probationAreaDropdown,
      createRecallRequest.probationArea,
      "probation area",
    )
    pageHelper.selectCheckboxValue(ualCheckbox, createRecallRequest.isInCustody.not())
    if (createRecallRequest.isInCustody) {
      pageHelper.selectDropdownOptionIfNotBlank(
        returnToCustodyNotificationMethodDropdown,
        returnToCustodyNotificationMethod,
        "return to custody notification method",
      )
    } else {
      val nextUalCheckDate = LocalDate.now().plusMonths(nextUalCheckMonths)
      pageHelper.enterDate(nextUalCheckInput, nextUalCheckDate)
    }
    pageHelper.selectDropdownOptionIfNotBlank(
      mappaLevelDropdown,
      createRecallRequest.mappaLevel,
      "mappa level",
    ) // Mappa level supposed to be populated automatically
    pageHelper.enterTextIfNotBlank(
      decisionFollowingBreachDateInput,
      createRecallRequest.decisionDateTime.format(dateTimeFormatter),
    )
    pageHelper.enterTextIfNotBlank(
      reportReceivedDateInput,
      createRecallRequest.receivedDateTime.format(dateTimeFormatter),
    )
    pageHelper.enterTextIfNotBlank(recommendedToDateInput, LocalDateTime.now().format(dateTimeFormatter))
    pageHelper.selectDropdownOptionIfNotBlank(policeForceDropdown, createRecallRequest.policeForce, "police force")
    pageHelper.selectDropdownOptionIfNotBlank(mandatoryDocumentsReceivedDropdown, "No", "mandatory documents received")
    checkAllMissingMandatoryDocuments()

    // Complete fields that have been updated/refreshed.
    pageHelper.waitForDropdownPopulation(driver, recommendedToOwnerDropdown)
    pageHelper.selectDropdownOptionIfNotBlank(
      recommendedToOwnerDropdown,
      createRecallRequest.recommendedToOwner,
      "recommended to owner",
    )
    pageHelper.waitForDropdownPopulation(driver, revocationIssuedByOwnerDropdown)
    pageHelper.selectDropdownOptionIfNotBlank(
      revocationIssuedByOwnerDropdown,
      revocationIssuedByOwner,
      "revocation issued by owner",
    )

    saveButton.click()
  }

  fun addDetailsMinute(createRecallRequest: CreateRecallRequest) {
    addMinute(contentCreator.generateRecallMinuteText(createRecallRequest))
  }

  fun addContrabandMinuteIfNeeded(createRecallRequest: CreateRecallRequest) {
    if (createRecallRequest.riskOfContrabandDetails.isNotBlank()) {
      addMinute(createRecallRequest.riskOfContrabandDetails)
    }
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed Creating Recall.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  fun extractCreatedRecallDetails(): CreatedRecall {
    // This should be performed when the Recall screen is in "existing recall" mode.
    // The add minute button is shown then, but not for a new recall
    if (addMinuteButton?.isDisplayed == true) {
      return CreatedRecall(id = extractRecallId())
    } else {
      throw AutomationException("Recall screen not refreshed")
    }
  }

  fun extractRecallDetails(): Recall {
    val nextUalCheckValue = nextUalCheckInput.getValue()
    return Recall(
      id = extractRecallId(),
      allMandatoryDocumentsReceived = Select(mandatoryDocumentsReceivedDropdown).firstSelectedOption.text,
      decisionDateTime = LocalDateTime.parse(decisionFollowingBreachDateInput.getValue(), dateTimeFormatter),
      isInCustody = ualCheckbox.isSelected.not(),
      mappaLevel = Select(mappaLevelDropdown).firstSelectedOption.text,
      nextUalCheck = if (nextUalCheckValue.isNotEmpty()) LocalDate.parse(nextUalCheckValue, dateFormatter) else null,
      owningTeam = owningTeamInput.getValue(),
      policeForce = Select(policeForceDropdown).firstSelectedOption.text,
      probationArea = Select(probationAreaDropdown).firstSelectedOption.text,
      recallType = Select(recallTypeDropdown).firstSelectedOption.text,
      receivedDateTime = LocalDateTime.parse(reportReceivedDateInput.getValue(), dateTimeFormatter),
      recommendedToDateTime = LocalDateTime.parse(recommendedToDateInput.getValue(), dateTimeFormatter),
      recommendedToOwner = recommendedToOwnerInput.getValue(),
      returnToCustodyNotificationMethod = Select(returnToCustodyNotificationMethodDropdown).firstSelectedOption.text,
      revocationIssuedByOwner = revocationIssuedByOwnerInput.getValue(),
    )
  }

  fun deleteRecall() {
    deleteButton.click()
    pageHelper.dismissConfirmDeleteAlert(driver)
  }

  fun urlFor(id: String): String {
    return urlPathTemplate.replace("{id}", id)
  }

  private fun checkAllMissingMandatoryDocuments() {
    pageHelper.selectCheckboxValue(missingPartACheckbox, true)
    pageHelper.selectCheckboxValue(missingOaSysCheckbox, true)
    pageHelper.selectCheckboxValue(missingPreSentenceReportCheckbox, true)
    pageHelper.selectCheckboxValue(missingPreviousConvictionsCheckbox, true)
    pageHelper.selectCheckboxValue(missingLicenceCheckbox, true)
    pageHelper.selectCheckboxValue(missingChargeSheetCheckbox, true)
  }

  private fun addMinute(text: String) {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.elementToBeClickable(By.id(addMinuteButtonId)))
    addMinuteButton?.click()
    minuteEditor.click()
    minuteEditor.sendKeys(text)
    saveMinuteButton.click()
  }

  private fun extractRecallId() = pageHelper.extractId(driver, "recall page")
}
