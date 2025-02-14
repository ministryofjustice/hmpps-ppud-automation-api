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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Document
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Minute
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.ContentCreator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
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
  @Value("\${ppud.recall.documentType.document}") private val documentTypeDocument: String,
  @Value("\${ppud.recall.documentType.email}") private val documentTypeEmail: String,
) {
  companion object {
    const val MINUTES_TEXT = "Minutes"
  }

  private val urlPathTemplate = "/Offender/Recall.aspx?data={id}"

  private val pageDescription = "recall page"

  @FindBy(id = "cntDetails_PageFooter1_cmdUploadDoc")
  private lateinit var uploadDocumentButton: WebElement

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

  @FindBy(id = "igtxtcntDetails_PageFooter1_docEdit_dteREPLY_ACTUAL")
  private lateinit var replyActualInput: WebElement

  @FindBy(id = "igtxtcntDetails_PageFooter1_docEdit_dteDELIVERY_ACTUAL")
  private lateinit var deliveryActualInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_txtDOCUMENT_TITLE")
  private lateinit var documentTitleInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_ddliDOCUMENT_TYPE")
  private lateinit var documentTypeDropdown: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_aceiOWNING_CASEWORKER_AutoCompleteTextBox")
  private lateinit var owningCaseworkerInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_aceiOWNING_CASEWORKER_AutoSelect")
  private lateinit var owningCaseworkerDropdown: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_fUpDocuments")
  private lateinit var chooseFileInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_cmdSaveAndAdd")
  private lateinit var saveAndAddMoreDocumentsButton: WebElement

  @FindBy(id = "cntDetails_PageFooter1_docEdit_cmdCancel")
  private lateinit var closeDocumentUploadButton: WebElement

  @FindBy(id = "UploadStatusData")
  private lateinit var documentUploadStatusTable: WebElement

  private val documentUploadStatuses: List<WebElement>
    get() = documentUploadStatusTable.findElements(By.xpath(".//td[starts-with(@id, 'upload_1')]"))

  // Recall Request Email is a mandatory document but doesn't have a checkbox
  private val missingMandatoryDocumentsMap: Map<DocumentCategory, WebElement> by lazy {
    mapOf(
      DocumentCategory.ChargeSheet to missingChargeSheetCheckbox,
      DocumentCategory.Licence to missingLicenceCheckbox,
      DocumentCategory.OASys to missingOaSysCheckbox,
      DocumentCategory.PartA to missingPartACheckbox,
      DocumentCategory.PreSentenceReport to missingPreSentenceReportCheckbox,
      DocumentCategory.PreviousConvictions to missingPreviousConvictionsCheckbox,
    )
  }

  private val documentTypeDescriptions: Map<DocumentType, String> by lazy {
    mapOf(
      DocumentType.Document to documentTypeDocument,
      DocumentType.Email to documentTypeEmail,
    )
  }

  private val documentsTable: WebElement?
    get() = driver.findElements(By.id("cntDetails_PageFooter1_GridView2")).firstOrNull()

  @FindBy(id = "minute_tree_container")
  private lateinit var minutesTreeContainer: WebElement

  @FindBy(id = "M_ctl00cntDetailsPageFooter1Minutes1UltraWebTree1")
  private lateinit var minutesTreeViewRoot: WebElement

  @FindBy(xpath = ".//*[@id='ctl00cntDetailsPageFooter1Minutes1UltraWebTree1_1']/span[text() = 'Minutes']")
  private lateinit var minutesNode: WebElement

  @FindBy(id = "cntDetails_PageFooter1_Minutes1_txtEditSubject")
  private lateinit var minuteSubjectInput: WebElement

  @FindBy(id = "MinuteDetail")
  private lateinit var minuteTextDiv: WebElement

  @FindBy(id = "cntDetails_PageFooter1_Minutes1_btnReplyTop")
  private lateinit var addMinuteButton: WebElement

  @FindBy(id = "cntDetails_PageFooter1_Minutes1_txtSubject")
  private lateinit var addMinuteSubjectInput: WebElement

  @FindBy(id = "cntDetails_PageFooter1_Minutes1_MinutesTextRich_tw")
  private lateinit var addMinuteEditor: WebElement

  private val saveMinuteButton: WebElement
    get() = driver.findElement(By.id("cntDetails_PageFooter1_Minutes1_btnSave"))

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun isMatching(receivedDateTime: LocalDateTime, recommendedTo: PpudUser): Boolean = reportReceivedDateInput.getValue() == receivedDateTime.format(dateTimeFormatter) &&
    recommendedToOwnerInput.getValue() == recommendedTo.fullName

  suspend fun createRecall(createRecallRequest: CreateRecallRequest) {
    // Complete these first as they trigger additional processing
    // Autocomplete box doesn't work with brackets
    pageHelper.enterTextIfNotBlank(recommendedToOwnerInput, createRecallRequest.recommendedTo.fullName)
    val revocationIssuedByOwnerFullName = removeTeamName(revocationIssuedByOwner)
    pageHelper.enterTextIfNotBlank(revocationIssuedByOwnerInput, revocationIssuedByOwnerFullName)

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
    pageHelper.selectDropdownOptionIfNotBlank(
      recommendedToOwnerDropdown,
      createRecallRequest.recommendedTo.formattedFullNameAndTeam,
      "recommended to owner",
    )
    pageHelper.selectDropdownOptionIfNotBlank(
      revocationIssuedByOwnerDropdown,
      revocationIssuedByOwner,
      "revocation issued by owner",
    )

    saveButton.click()
  }

  fun uploadMandatoryDocument(request: UploadMandatoryDocumentRequest, filepath: String) {
    uploadDocument(request.category.documentType, request.category.title, request.owningCaseworker, filepath)
  }

  fun markMandatoryDocumentAsReceived(documentCategory: DocumentCategory) {
    val missingCheckbox = missingMandatoryDocumentsMap[documentCategory]
    if (missingCheckbox != null) {
      if (missingCheckbox.isDisplayed) {
        pageHelper.selectCheckboxValue(missingCheckbox, false)
      }
      if (extractMissingMandatoryDocuments().isEmpty()) {
        pageHelper.selectDropdownOptionIfNotBlank(
          mandatoryDocumentsReceivedDropdown,
          "Yes",
          "mandatory documents received",
        )
      }
      saveButton.click()
    }
  }

  fun uploadAdditionalDocument(request: UploadAdditionalDocumentRequest, filepath: String) {
    // We're setting document type to document for now so that we don't have to
    // ask the user what type it is
    uploadDocument(DocumentType.Document, request.title, request.owningCaseworker, filepath)
  }

  fun hasMatchingMinute(subject: String, text: String): Boolean = extractMinutes().stream().anyMatch { it.subject == subject && it.text == text }

  fun addMinute(subject: String, text: String) {
    addMinuteInternal(subject = subject, text = text)
  }

  fun addContrabandMinuteIfNeeded(createRecallRequest: CreateRecallRequest) {
    if (createRecallRequest.riskOfContrabandDetails.isNotBlank()) {
      addMinuteInternal(createRecallRequest.riskOfContrabandDetails, subject = "RISK OF CONTRABAND")
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
    if (addMinuteButton.isDisplayed) {
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
      missingMandatoryDocuments = extractMissingMandatoryDocuments(),
      minutes = extractMinutes(),
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
      documents = extractDocuments(),
    )
  }

  fun urlFor(id: String): String = urlPathTemplate.replace("{id}", id)

  private fun checkAllMissingMandatoryDocuments() {
    missingMandatoryDocumentsMap.forEach {
      pageHelper.selectCheckboxValue(it.value, true)
    }
  }

  private fun extractMissingMandatoryDocuments(): List<DocumentCategory> = if (Select(mandatoryDocumentsReceivedDropdown).firstSelectedOption.text == "No") {
    missingMandatoryDocumentsMap.mapNotNull { if (it.value.isSelected) it.key else null }
  } else {
    emptyList()
  }

  private fun extractDocuments(): List<Document> = if (documentsTable != null) {
    val rows = documentsTable!!.findElements(By.xpath(".//tr[position()>1]"))
    rows.map {
      Document(
        title = it.findElement(By.xpath(".//td[3]")).text,
        documentType = it.findElement(By.xpath(".//td[4]")).text,
        owningCaseworker = it.findElement(By.xpath(".//td[12]")).text,
      )
    }
  } else {
    emptyList()
  }

  private fun extractMinutes(): List<Minute> {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.textToBePresentInElement(minutesTreeContainer, MINUTES_TEXT))
    val treeView = TreeView(minutesTreeViewRoot)
    val minuteEntryNodes = if (treeView.nodeWithTextIsExpandable(MINUTES_TEXT)) treeView.expandNodeWithText(MINUTES_TEXT).children() else emptyList()
    return minuteEntryNodes.map {
      it.click()
      Minute(
        subject = minuteSubjectInput.getValue(),
        text = minuteTextDiv.text,
      )
    }
  }

  private fun uploadDocument(documentType: DocumentType, title: String, owningCaseworker: PpudUser, filepath: String) {
    val today = LocalDate.now().format(dateFormatter)
    uploadDocumentButton.click()
    owningCaseworkerInput.click()
    owningCaseworkerInput.sendKeys(owningCaseworker.fullName)
    deliveryActualInput.sendKeys(today)
    pageHelper.selectDropdownOptionIfNotBlank(
      documentTypeDropdown,
      documentTypeDescriptions[documentType],
      "document type",
    )
    documentTitleInput.sendKeys(title)
    replyActualInput.sendKeys(today)
    chooseFileInput.sendKeys(filepath)
    pageHelper.selectDropdownOptionIfNotBlank(
      owningCaseworkerDropdown,
      owningCaseworker.formattedFullNameAndTeam,
      "owning caseworker",
    )
    saveAndAddMoreDocumentsButton.click()
    waitForDocumentToUpload()
    closeDocumentUploadButton.click()
  }

  private fun waitForDocumentToUpload() {
    WebDriverWait(driver, Duration.ofSeconds(30))
      .until { documentUploadStatuses.all { it.text == "Complete" } }
  }

  private fun addMinuteInternal(text: String, subject: String = "") {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.elementToBeClickable(addMinuteButton))
    minutesNode.click()
    addMinuteButton.click()
    pageHelper.enterTextIfNotBlank(addMinuteSubjectInput, subject)
    addMinuteEditor.click()
    addMinuteEditor.sendKeys(text)
    saveMinuteButton.click()
  }

  private fun extractRecallId() = pageHelper.extractId(pageDescription)

  private fun removeTeamName(nameWithTeam: String) = nameWithTeam.takeWhile { (it == '(').not() }
}
