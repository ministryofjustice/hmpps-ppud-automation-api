package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.InvalidOffenderIdException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.dismissCheckCapitalisationAlert
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.dismissConfirmDeleteAlert
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeViewNode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.enterTextIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectCheckboxValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.YoungOffenderCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class OffenderPage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
  private val youngOffenderCalculator: YoungOffenderCalculator,
  @Value("\${ppud.url}") private val ppudUrl: String,
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

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]/td[1]")
  private lateinit var addressHistoryPremises: WebElement

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]/td[2]")
  private lateinit var addressHistoryLine1: WebElement

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]/td[3]")
  private lateinit var addressHistoryLine2: WebElement

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]/td[4]")
  private lateinit var addressHistoryPostcode: WebElement

  @FindBy(xpath = "//*[@id=\"$ADDRESS_HISTORY_TABLE_ID\"]//tr[last()]/td[5]")
  private lateinit var addressHistoryPhoneNumber: WebElement

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

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun viewOffenderWithId(offenderId: String) {
    driver.navigate().to("$ppudUrl/Offender/PersonalDetails.aspx?data=$offenderId")
    throwIfInvalidOffenderId()
  }

  fun navigateToNewRecallFor(sentenceDate: LocalDate, releaseDate: LocalDate) {
    navigateToRecallsFor(sentenceDate, releaseDate)
      .findNodeWithTextContaining("New")
      .click()
  }

  fun updateOffender(updateOffenderRequest: UpdateOffenderRequest) {
    // Complete these first as they trigger additional processing
    selectCheckboxValue(ualCheckbox, updateOffenderRequest.isInCustody.not())

    // Complete standalone fields
    enterAddress(updateOffenderRequest.address)
    croOtherNumberInput.clear()
    croOtherNumberInput.enterTextIfNotBlank(updateOffenderRequest.croNumber)
    dateOfBirthInput.click()
    dateOfBirthInput.sendKeys(updateOffenderRequest.dateOfBirth.format(dateFormatter))
    selectDropdownOptionIfNotBlank(ethnicityDropdown, updateOffenderRequest.ethnicity, "ethnicity")
    familyNameInput.clear()
    familyNameInput.sendKeys(updateOffenderRequest.familyName)
    dismissCheckCapitalisationAlert(driver, nomsIdInput)
    firstNamesInput.clear()
    firstNamesInput.sendKeys(updateOffenderRequest.firstNames)
    dismissCheckCapitalisationAlert(driver, nomsIdInput)
    selectDropdownOptionIfNotBlank(genderDropdown, updateOffenderRequest.gender, "gender")
    selectDropdownOptionIfNotBlank(immigrationStatusDropdown, immigrationStatus, "immigration status")
    nomsIdInput.clear()
    nomsIdInput.sendKeys(updateOffenderRequest.nomsId)
    selectDropdownOptionIfNotBlank(prisonerCategoryDropdown, prisonerCategory, "prisoner category")
    prisonNumberInput.clear()
    prisonNumberInput.sendKeys(updateOffenderRequest.prisonNumber)
    selectDropdownOptionIfNotBlank(statusDropdown, status, "status")
    if (youngOffenderCalculator.isYoungOffender(updateOffenderRequest.dateOfBirth)) {
      selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderYes, "young offender")
    } else {
      selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderNo, "young offender")
    }

    saveButton.click()
  }

  suspend fun deleteOffender() {
    deleteButton.click()
    dismissConfirmDeleteAlert(driver)
  }

  fun extractRecallLinks(sentenceDate: LocalDate, releaseDate: LocalDate): List<String> {
    return navigateToRecallsFor(sentenceDate, releaseDate)
      .children()
      .filter { it.text.startsWith("New").not() }
      .map { it.getAttribute("igurl") }
  }

  fun extractCreatedOffenderDetails(): CreatedOffender {
    return CreatedOffender(
      id = extractId(),
    )
  }

  fun extractSearchResultOffenderDetails(): SearchResultOffender {
    return SearchResultOffender(
      id = extractId(),
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
      id = extractId(),
      address = extractAddress(),
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

  private fun throwIfInvalidOffenderId() {
    if (driver.title.equals("url checksum error", ignoreCase = true)) {
      throw InvalidOffenderIdException("Offender ID is invalid. Checksum validation failed.")
    }
  }

  private fun navigateToRecallsFor(sentenceDate: LocalDate, releaseDate: LocalDate): TreeViewNode {
    return TreeView(navigationTreeViewRoot)
      .expandNodeWithText("Sentences")
      .expandNodeWithTextContaining(sentenceDate.format(dateFormatter))
      .expandNodeWithText("Releases")
      .expandNodeWithTextContaining(releaseDate.format(dateFormatter))
      .expandNodeWithTextContaining("Recalls")
  }

  private fun determineSentenceLinks(): List<String> {
    return TreeView(navigationTreeViewRoot)
      .expandNodeWithText("Sentences")
      .children()
      .filter { it.text.startsWith("New").not() }
      .map { it.getAttribute("igurl") }
  }

  private fun extractId(): String {
    val url = driver.currentUrl
    val idMatch = Regex(".+?data=(.+)").find(url)
      ?: throw AutomationException("Expected the existing offender page but URL was '$url'")
    val (id) = idMatch.destructured
    return id
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
      OffenderAddress(
        premises = addressHistoryPremises.text.trim(),
        line1 = addressHistoryLine1.text.trim(),
        line2 = addressHistoryLine2.text.trim(),
        postcode = addressHistoryPostcode.text.trim(),
        phoneNumber = addressHistoryPhoneNumber.text.trim(),
      )
    } else {
      OffenderAddress()
    }
    cancelAddressHistoryButton.click()

    return address
  }
}
