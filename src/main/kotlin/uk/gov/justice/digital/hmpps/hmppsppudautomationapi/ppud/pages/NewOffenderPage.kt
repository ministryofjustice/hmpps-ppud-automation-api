package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.enterTextIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.YoungOffenderCalculator
import java.time.Duration
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class NewOffenderPage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
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

  @FindBy(id = "content_aceINDEX_OFFENCE_AutoSelect")
  private lateinit var indexOffenceDropdown: WebElement

  @FindBy(id = "content_aceINDEX_OFFENCE_AutoCompleteTextBox")
  private lateinit var indexOffenceInput: WebElement

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
    selectDropdownOptionIfNotBlank(custodyTypeDropdown, createOffenderRequest.custodyType, "custody type")

    // Complete standalone fields
    croNumberInput.enterTextIfNotBlank(createOffenderRequest.croNumber)
    dateOfBirthInput.click()
    dateOfBirthInput.sendKeys(createOffenderRequest.dateOfBirth.format(dateFormatter))
    dateOfSentenceInput.click()
    dateOfSentenceInput.enterTextIfNotBlank(createOffenderRequest.dateOfSentence.format(dateFormatter))
    selectDropdownOptionIfNotBlank(ethnicityDropdown, createOffenderRequest.ethnicity, "ethnicity")
    familyNameInput.sendKeys(createOffenderRequest.familyName)
    dismissCheckCapitalisationAlert()
    firstNamesInput.sendKeys(createOffenderRequest.firstNames)
    dismissCheckCapitalisationAlert()
    selectDropdownOptionIfNotBlank(genderDropdown, createOffenderRequest.gender, "gender")
    selectDropdownOptionIfNotBlank(immigrationStatusDropdown, immigrationStatus, "immigration status")
    nomsIdInput.sendKeys(createOffenderRequest.nomsId)
    prisonNumberInput.sendKeys(createOffenderRequest.prisonNumber)
    selectDropdownOptionIfNotBlank(prisonerCategoryDropdown, prisonerCategory, "prison category")
    selectDropdownOptionIfNotBlank(statusDropdown, status, "status")
    if (youngOffenderCalculator.isYoungOffender(createOffenderRequest.dateOfBirth)) {
      selectDropdownOptionIfNotBlank(youngOffenderDropdown, youngOffenderYes, "young offender")
    }

    // Complete fields that have been updated/refreshed.
    selectDropdownOptionIfNotBlank(indexOffenceDropdown, createOffenderRequest.indexOffence, "index offence")
    selectDropdownOptionIfNotBlank(mappaLevelDropdown, createOffenderRequest.mappaLevel, "mappa level")

    saveButton.click()
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw Exception("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  private fun dismissCheckCapitalisationAlert() {
    try {
      nomsIdInput.click()
      val alert = driver.switchTo().alert()
      if (alert.text.contains("check that the capitalisation is correct")) {
        alert.accept()
      } else {
        throw Exception("Alert shown with the text '${alert.text}")
      }
    } catch (ex: NoAlertPresentException) {
      // No alert so we can proceed
    }
  }
}
