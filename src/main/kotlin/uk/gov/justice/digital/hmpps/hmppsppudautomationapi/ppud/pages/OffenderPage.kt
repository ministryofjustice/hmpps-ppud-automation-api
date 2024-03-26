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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.InvalidOffenderIdException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.ContentCreator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.YoungOffenderCalculator
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
internal class OffenderPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
  private val dateFormatter: DateTimeFormatter,
  private val contentCreator: ContentCreator,
  private val youngOffenderCalculator: YoungOffenderCalculator,
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.offender.caseworker.inCustody}") private val caseworkerInCustody: String,
  @Value("\${ppud.offender.caseworker.ual}") private val caseworkerUal: String,
  @Value("\${ppud.offender.immigrationStatus}") private val immigrationStatus: String,
  @Value("\${ppud.offender.prisonerCategory}") private val prisonerCategory: String,
  @Value("\${ppud.offender.status}") private val status: String,
  @Value("\${ppud.offender.youngOffenderYes}") private val youngOffenderYes: String,
  @Value("\${ppud.offender.youngOffenderNo}") private val youngOffenderNo: String,
) {

  companion object {
    private const val ADDRESS_HISTORY_TABLE_ID = "cntDetails_GridView2"
  }

  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_PageFooter1_cmdDelete")
  private lateinit var deleteButton: WebElement

  @FindBy(id = "cntDetails_btnView")
  private lateinit var viewAddressHistoryButton: WebElement

  @FindBy(id = "cntDetails_cmdCancelView")
  private lateinit var cancelAddressHistoryButton: WebElement

  private val addressHistoryTable: WebElement?
    get() = driver.findElements(By.id(ADDRESS_HISTORY_TABLE_ID)).firstOrNull()

  private val addressHistoryLastPageLink: WebElement?
    get() = driver.findElements(By.xpath("//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]//a")).lastOrNull()

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]")
  private lateinit var addressHistoryLastRow: WebElement

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()-1]")
  private lateinit var addressHistoryPagedLastRow: WebElement

  private val addressHistoryTableCellsXpath = "./td"

  @FindBy(id = "cntDetails_btnEditAddress")
  private lateinit var editAddressButton: WebElement

  @FindBy(id = "cntDetails_cmdChange")
  private lateinit var editAddressSubmitButton: WebElement

  @FindBy(id = "cntDetails_Premises")
  private lateinit var addressPremisesInput: WebElement

  @FindBy(id = "cntDetails_Line1")
  private lateinit var addressLine1Input: WebElement

  @FindBy(id = "cntDetails_Line2")
  private lateinit var addressLine2Input: WebElement

  @FindBy(id = "cntDetails_Postcode")
  private lateinit var addressPostcodeInput: WebElement

  @FindBy(id = "cntDetails_Phone")
  private lateinit var addressPhoneNumberInput: WebElement

  @FindBy(id = "cntDetails_aceiOWNING_CASEWORKER_AutoCompleteTextBox")
  private lateinit var caseworkerInput: WebElement

  @FindBy(id = "cntDetails_aceiOWNING_CASEWORKER_AutoSelect")
  private lateinit var caseworkerDropdown: WebElement

  @FindBy(id = "cntDetails_txtGENERAL_COMMENTS")
  private lateinit var commentsTextArea: WebElement

  @FindBy(id = "cntDetails_txtCRO_PNC")
  private lateinit var croOtherNumberInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOB")
  private lateinit var dateOfBirthInput: WebElement

  @FindBy(id = "cntDetails_ddliETHNICITY")
  private lateinit var ethnicityDropdown: WebElement

  @FindBy(id = "cntDetails_txtFIRST_NAMES")
  private lateinit var firstNamesInput: WebElement

  @FindBy(id = "cntDetails_txtFAMILY_NAME")
  private lateinit var familyNameInput: WebElement

  @FindBy(id = "cntDetails_ddlsGENDER")
  private lateinit var genderDropdown: WebElement

  @FindBy(id = "cntDetails_ddliIMMIGRATION_STATUS")
  private lateinit var immigrationStatusDropdown: WebElement

  @FindBy(id = "M_ctl00treetvOffender")
  private lateinit var navigationTreeViewRoot: WebElement

  @FindBy(id = "cntDetails_txtNOMS_ID")
  private lateinit var nomsIdInput: WebElement

  @FindBy(id = "cntDetails_ddliPRISONER_CATEGORY")
  private lateinit var prisonerCategoryDropdown: WebElement

  @FindBy(id = "cntDetails_txtPRISON_NUMBER")
  private lateinit var prisonNumberInput: WebElement

  @FindBy(id = "cntDetails_chkUAL_FLAG")
  private lateinit var ualCheckbox: WebElement

  @FindBy(id = "cntDetails_ddliYOUNG_OFFENDER")
  private lateinit var youngOffenderDropdown: WebElement

  @FindBy(id = "cntDetails_ddliSTATUS")
  private lateinit var statusDropdown: WebElement

  private val caseworkers = mapOf(Pair(true, caseworkerInCustody), Pair(false, caseworkerUal))

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun viewOffenderWithId(offenderId: String) {
    driver.navigate().to("$ppudUrl/Offender/PersonalDetails.aspx?data=$offenderId")
    throwIfErrorViewingOffender()
  }

  fun verifyOn() {
    WebDriverWait(driver, Duration.ofSeconds(10))
      .until(
        ExpectedConditions.and(
          ExpectedConditions.urlContains("PersonalDetails.aspx"),
          ExpectedConditions.visibilityOf(prisonNumberInput),
        ),
      )
  }

  fun updateOffender(updateOffenderRequest: UpdateOffenderRequest) {
    // Complete first as additional processing is triggered
    pageHelper.selectCheckboxValue(ualCheckbox, updateOffenderRequest.isInCustody.not())
    enterCaseworkerText(updateOffenderRequest.isInCustody)

    // Complete standalone fields
    enterAddress(updateOffenderRequest.address)
    enterAdditionalAddresses(updateOffenderRequest.additionalAddresses)
    pageHelper.enterTextIfNotBlank(croOtherNumberInput, updateOffenderRequest.croNumber)
    dateOfBirthInput.click()
    dateOfBirthInput.sendKeys(updateOffenderRequest.dateOfBirth.format(dateFormatter))
    pageHelper.selectDropdownOptionIfNotBlank(ethnicityDropdown, updateOffenderRequest.ethnicity, "ethnicity")
    familyNameInput.clear()
    familyNameInput.sendKeys(updateOffenderRequest.familyName)
    pageHelper.dismissCheckCapitalisationAlert(nomsIdInput)
    firstNamesInput.clear()
    firstNamesInput.sendKeys(updateOffenderRequest.firstNames)
    pageHelper.dismissCheckCapitalisationAlert(nomsIdInput)
    pageHelper.selectDropdownOptionIfNotBlankIgnoringSpaces(genderDropdown, updateOffenderRequest.gender, "gender")
    pageHelper.selectDropdownOptionIfNotBlank(immigrationStatusDropdown, immigrationStatus, "immigration status")
    nomsIdInput.clear()
    nomsIdInput.sendKeys(updateOffenderRequest.nomsId)
    pageHelper.selectDropdownOptionIfNotBlank(prisonerCategoryDropdown, prisonerCategory, "prisoner category")
    prisonNumberInput.clear()
    prisonNumberInput.sendKeys(updateOffenderRequest.prisonNumber)
    pageHelper.selectDropdownOptionIfNotBlank(statusDropdown, status, "status")
    if (youngOffenderCalculator.isYoungOffender(updateOffenderRequest.dateOfBirth)) {
      pageHelper.selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderYes, "young offender")
    } else {
      pageHelper.selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderNo, "young offender")
    }

    // Complete fields that have been updated/refreshed.
    selectCaseworkerMatch(updateOffenderRequest.isInCustody)

    saveButton.click()
  }

  fun updateAdditionalAddresses(additionalAddresses: List<OffenderAddress>) {
    enterAdditionalAddresses(additionalAddresses)
    saveButton.click()
  }

  fun deleteOffender() {
    deleteButton.click()
    pageHelper.dismissConfirmDeleteAlert()
  }

  fun extractCreatedOffenderDetails(sentenceExtractor: (String) -> CreatedSentence): CreatedOffender {
    return CreatedOffender(
      id = extractOffenderId(),
      // Do sentence last because it navigates away
      sentence = sentenceExtractor(determineSentenceLinks().first()),
    )
  }

  fun extractSearchResultOffenderDetails(): SearchResultOffender {
    return SearchResultOffender(
      id = extractOffenderId(),
      croNumber = croOtherNumberInput.getValue(),
      croOtherNumber = croOtherNumberInput.getValue(),
      nomsId = nomsIdInput.getValue(),
      firstNames = firstNamesInput.getValue(),
      familyName = familyNameInput.getValue(),
      dateOfBirth = LocalDate.parse(dateOfBirthInput.getValue(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
    )
  }

  fun extractOffenderDetails(sentenceExtractor: (List<String>) -> List<Sentence>): Offender {
    return Offender(
      id = extractOffenderId(),
      address = extractAddress(),
      caseworker = caseworkerInput.getValue(),
      comments = commentsTextArea.getValue(),
      croOtherNumber = croOtherNumberInput.getValue(),
      dateOfBirth = LocalDate.parse(dateOfBirthInput.getValue(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      ethnicity = Select(ethnicityDropdown).firstSelectedOption.text,
      familyName = familyNameInput.getValue(),
      firstNames = firstNamesInput.getValue(),
      gender = Select(genderDropdown).firstSelectedOption.text,
      immigrationStatus = Select(immigrationStatusDropdown).firstSelectedOption.text,
      isInCustody = ualCheckbox.isSelected.not(),
      nomsId = nomsIdInput.getValue(),
      prisonerCategory = Select(prisonerCategoryDropdown).firstSelectedOption.text,
      prisonNumber = prisonNumberInput.getValue(),
      youngOffender = Select(youngOffenderDropdown).firstSelectedOption.text,
      status = Select(statusDropdown).firstSelectedOption.text,
      // Do sentences last because it navigates away
      sentences = sentenceExtractor(determineSentenceLinks()),
    )
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  private fun throwIfErrorViewingOffender() {
    if (driver.title.equals("url checksum error", ignoreCase = true)) {
      throw InvalidOffenderIdException("Offender ID is invalid. Checksum validation failed.")
    }
    if (driver.title.equals("invalid url", ignoreCase = true)) {
      throw InvalidOffenderIdException("Attempted to navigate to an invalid URL. This is probably due to an invalid offender ID.")
    }
    if (pageHelper.isCustomErrorUrl()) {
      throw AutomationException("Unable to view offender. An error occurred in PPUD.")
    }
  }

  private fun determineSentenceLinks(): List<String> {
    return TreeView(navigationTreeViewRoot)
      .expandNodeWithText("Sentences")
      .children()
      .filter { it.text.startsWith("New").not() }
      .map { it.getAttribute("igurl") }
  }

  private fun extractOffenderId() = pageHelper.extractId("existing offender page")

  private fun enterCaseworkerText(isInCustody: Boolean) {
    val caseworker = caseworkers.getValue(isInCustody)
    val caseworkerSearchable = caseworker.takeWhile { (it == '(').not() }
    pageHelper.enterTextIfNotBlank(caseworkerInput, caseworkerSearchable)
  }

  private fun selectCaseworkerMatch(isInCustody: Boolean) {
    val caseworker = caseworkers.getValue(isInCustody)
    pageHelper.selectDropdownOptionIfNotBlank(caseworkerDropdown, caseworker, "caseworker")
  }

  private fun enterAddress(address: OffenderAddress) {
    editAddressButton.click()
    addressPremisesInput.sendKeys(address.premises)
    addressLine1Input.sendKeys(address.line1)
    addressLine2Input.sendKeys(address.line2)
    addressPostcodeInput.sendKeys(address.postcode)
    addressPhoneNumberInput.sendKeys(address.phoneNumber)
    editAddressSubmitButton.click()
  }

  private fun extractAddress(): OffenderAddress {
    viewAddressHistoryButton.click()
    val address = if (addressHistoryTable != null) {
      extractAddressFromRow(lastAddressRow())
    } else {
      OffenderAddress()
    }
    cancelAddressHistoryButton.click()

    return address
  }

  private fun lastAddressRow(): WebElement =
    if (addressHistoryLastPageLink != null) {
      addressHistoryLastPageLink?.click()
      viewAddressHistoryButton.click()
      addressHistoryPagedLastRow
    } else {
      addressHistoryLastRow
    }

  private fun extractAddressFromRow(tableRow: WebElement): OffenderAddress {
    val cells = tableRow.findElements(By.xpath(addressHistoryTableCellsXpath))
    return OffenderAddress(
      premises = cells[0].text.trim(),
      line1 = cells[1].text.trim(),
      line2 = cells[2].text.trim(),
      postcode = cells[3].text.trim(),
      phoneNumber = cells[4].text.trim(),
    )
  }

  private fun enterAdditionalAddresses(additionalAddresses: List<OffenderAddress>) {
    val currentComments = commentsTextArea.getValue()
    commentsTextArea.clear()
    commentsTextArea.sendKeys(
      contentCreator.addAdditionalAddressesToComments(additionalAddresses, currentComments),
    )
  }
}
