package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.YoungOffenderCalculator
import java.time.Duration

@Component
internal class NewOffenderPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
  private val youngOffenderCalculator: YoungOffenderCalculator,
  @Value("\${ppud.offender.immigrationStatus}") private val immigrationStatus: String,
  @Value("\${ppud.offender.prisonerCategory}") private val prisonerCategory: String,
  @Value("\${ppud.offender.status}") private val status: String,
  @Value("\${ppud.offender.youngOffenderYes}") private val youngOffenderYes: String,
) {

  private val title = "New Offender"

  @FindBy(id = "content_cmdSave1")
  private lateinit var saveButton: WebElement

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("content_valSummary")).firstOrNull()

  private val errorLabel: WebElement?
    get() = driver.findElements(By.id("content_lblError")).firstOrNull()

  private val duplicatePanel: WebElement?
    get() = driver.findElements(By.id("content_panelDuplicate")).firstOrNull()

  @FindBy(id = "content_txtPREMISES")
  private lateinit var addressPremisesInput: WebElement

  @FindBy(id = "content_txtLINE_1")
  private lateinit var addressLine1Input: WebElement

  @FindBy(id = "content_txtLINE_2")
  private lateinit var addressLine2Input: WebElement

  @FindBy(id = "content_txtPOSTCODE")
  private lateinit var addressPostcodeInput: WebElement

  @FindBy(id = "content_txtTELEPHONE")
  private lateinit var addressPhoneNumberInput: WebElement

  @FindBy(id = "content_txtCRO_PNC")
  private lateinit var croNumberInput: WebElement

  @FindBy(id = "content_ddlCustodyType")
  private lateinit var custodyTypeDropdown: WebElement

  @FindBy(id = "igtxtcontent_dteDOB")
  private lateinit var dateOfBirthInput: WebElement

  @FindBy(id = "igtxtcontent_dtpDateOfSentence")
  private lateinit var dateOfSentenceInput: WebElement

  @FindBy(id = "content_ddlEthnicity")
  private lateinit var ethnicityDropdown: WebElement

  @FindBy(id = "content_txtFamilyName")
  private lateinit var familyNameInput: WebElement

  @FindBy(id = "content_txtFirstName")
  private lateinit var firstNamesInput: WebElement

  @FindBy(id = "content_ddlGender")
  private lateinit var genderDropdown: WebElement

  @FindBy(id = "content_ddlIMMIGRATION_STATUS")
  private lateinit var immigrationStatusDropdown: WebElement

  private val indexOffenceDropdown: WebElement by lazy { driver.findElement(By.id("content_aceINDEX_OFFENCE_AutoSelect")) }

  private val indexOffenceInput: WebElement by lazy { driver.findElement(By.id("content_aceINDEX_OFFENCE_AutoCompleteTextBox")) }

  @FindBy(id = "content_ddlMappaLevel")
  private lateinit var mappaLevelDropdown: WebElement

  @FindBy(id = "content_txtNOMS_ID")
  private lateinit var nomsIdInput: WebElement

  @FindBy(id = "content_ddlPrisonerCategory")
  private lateinit var prisonerCategoryDropdown: WebElement

  @FindBy(id = "content_txtPrisonNumber")
  private lateinit var prisonNumberInput: WebElement

  @FindBy(id = "content_ddlStatus")
  private lateinit var statusDropdown: WebElement

  @FindBy(id = "content_chkUAL_FLAG")
  private lateinit var ualCheckbox: WebElement

  @FindBy(id = "content_ddliYOUNG_OFFENDER")
  private lateinit var youngOffenderDropdown: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun verifyOn(): NewOffenderPage {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
    return this
  }

  suspend fun createOffender(createOffenderRequest: CreateOffenderRequest) {
    // Complete these first as they trigger additional processing
    indexOffenceInput.click()
    indexOffenceInput.sendKeys(createOffenderRequest.indexOffence)
    pageHelper.selectDropdownOptionIfNotBlank(custodyTypeDropdown, createOffenderRequest.custodyType, "custody type")

    // Complete standalone fields
    enterAddress(createOffenderRequest.address)
    pageHelper.enterTextIfNotBlank(croNumberInput, createOffenderRequest.croNumber)
    pageHelper.enterDate(dateOfBirthInput, createOffenderRequest.dateOfBirth)
    pageHelper.enterDate(dateOfSentenceInput, createOffenderRequest.dateOfSentence)
    pageHelper.selectDropdownOptionIfNotBlank(ethnicityDropdown, createOffenderRequest.ethnicity, "ethnicity")
    familyNameInput.sendKeys(createOffenderRequest.familyName)
    pageHelper.dismissCheckCapitalisationAlert(nomsIdInput)
    firstNamesInput.sendKeys(createOffenderRequest.firstNames)
    pageHelper.dismissCheckCapitalisationAlert(nomsIdInput)
    pageHelper.selectDropdownOptionIfNotBlankIgnoringSpaces(genderDropdown, createOffenderRequest.gender, "gender")
    pageHelper.selectDropdownOptionIfNotBlank(immigrationStatusDropdown, immigrationStatus, "immigration status")
    nomsIdInput.sendKeys(createOffenderRequest.nomsId)
    prisonNumberInput.sendKeys(createOffenderRequest.prisonNumber)
    pageHelper.selectDropdownOptionIfNotBlank(prisonerCategoryDropdown, prisonerCategory, "prisoner category")
    pageHelper.selectDropdownOptionIfNotBlank(statusDropdown, status, "status")
    pageHelper.selectCheckboxValue(ualCheckbox, createOffenderRequest.isInCustody.not())
    if (youngOffenderCalculator.isYoungOffender(createOffenderRequest.dateOfBirth)) {
      pageHelper.selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderYes, "young offender")
    }

    // Complete fields that have been updated/refreshed.
    pageHelper.selectDropdownOptionIfNotBlank(indexOffenceDropdown, createOffenderRequest.indexOffence, "index offence")
    pageHelper.selectDropdownOptionIfNotBlank(mappaLevelDropdown, createOffenderRequest.mappaLevel, "mappa level")

    saveButton.click()
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }

    if (errorLabel?.text?.isNotBlank() == true) {
      throw AutomationException("Offender creation failed. ${errorLabel?.text}")
    }

    if (duplicatePanel?.isDisplayed == true) {
      throw AutomationException("Duplicate details found on PPUD for this offender.")
    }
  }

  private fun enterAddress(address: OffenderAddress) {
    addressPremisesInput.sendKeys(address.premises)
    addressLine1Input.sendKeys(address.line1)
    addressLine2Input.sendKeys(address.line2)
    addressPostcodeInput.sendKeys(address.postcode)
    addressPhoneNumberInput.sendKeys(address.phoneNumber)
  }
}
