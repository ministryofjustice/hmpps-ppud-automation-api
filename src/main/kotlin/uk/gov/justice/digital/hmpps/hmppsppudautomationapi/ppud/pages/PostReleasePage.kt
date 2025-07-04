package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Contact
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.ContactWithTelephone
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.PostRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue

@Component
internal class PostReleasePage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
) {
  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_txtACO")
  private lateinit var assistantChiefOfficerInput: WebElement

  @FindBy(id = "cntDetails_txtACO_FAX_EMAIL")
  private lateinit var assistantChiefOfficerFaxEmailInput: WebElement

  @FindBy(id = "cntDetails_ddliTYPE_OF_LICENCE")
  private lateinit var licenceTypeDropdown: WebElement

  @FindBy(id = "cntDetails_txtOFF_MANAGER")
  private lateinit var offenderManagerInput: WebElement

  @FindBy(id = "cntDetails_txtOM_FAX_EMAIL")
  private lateinit var offenderManagerFaxEmailInput: WebElement

  @FindBy(id = "cntDetails_txtOM_PHONE")
  private lateinit var offenderManagerTelephoneInput: WebElement

  @FindBy(id = "cntDetails_ddliPROBATION_SERVICE")
  private lateinit var probationServiceDropdown: WebElement

  @FindBy(id = "cntDetails_txtSPOC")
  private lateinit var spocInput: WebElement

  @FindBy(id = "cntDetails_txtSPOC_FAX_EMAIL")
  private lateinit var spocFaxEmailInput: WebElement

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun navigateToPostReleaseFor(releaseId: String) {
    navigationTreeViewComponent
      .findPostReleaseNodeFor(releaseId)
      .click()
  }

  fun updatePostRelease(updatePostReleaseRequest: UpdatePostReleaseRequest, licenceType: String) {
    assistantChiefOfficerInput.clear()
    assistantChiefOfficerInput.sendKeys(updatePostReleaseRequest.assistantChiefOfficer.name)
    assistantChiefOfficerFaxEmailInput.clear()
    assistantChiefOfficerFaxEmailInput.sendKeys(updatePostReleaseRequest.assistantChiefOfficer.faxEmail)
    pageHelper.selectDropdownOptionIfNotBlank(licenceTypeDropdown, licenceType, "licence type")
    offenderManagerInput.clear()
    offenderManagerInput.sendKeys(updatePostReleaseRequest.offenderManager.name)
    offenderManagerFaxEmailInput.clear()
    offenderManagerFaxEmailInput.sendKeys(updatePostReleaseRequest.offenderManager.faxEmail)
    offenderManagerTelephoneInput.clear()
    offenderManagerTelephoneInput.sendKeys(updatePostReleaseRequest.offenderManager.telephone)
    pageHelper.selectDropdownOptionIfNotBlank(
      probationServiceDropdown,
      updatePostReleaseRequest.probationService,
      "probation service",
    )
    spocInput.clear()
    spocInput.sendKeys(updatePostReleaseRequest.spoc.name)
    spocFaxEmailInput.clear()
    spocFaxEmailInput.sendKeys(updatePostReleaseRequest.spoc.faxEmail)

    saveButton.click()
  }

  fun extractPostReleaseDetails(): PostRelease = PostRelease(
    assistantChiefOfficer = Contact(
      name = assistantChiefOfficerInput.getValue(),
      faxEmail = assistantChiefOfficerFaxEmailInput.getValue(),
    ),
    licenceType = Select(licenceTypeDropdown).firstSelectedOption.text,
    offenderManager = ContactWithTelephone(
      name = offenderManagerInput.getValue(),
      faxEmail = offenderManagerFaxEmailInput.getValue(),
      telephone = offenderManagerTelephoneInput.getValue(),
    ),
    probationService = Select(probationServiceDropdown).firstSelectedOption.text,
    spoc = Contact(
      name = spocInput.getValue(),
      faxEmail = spocFaxEmailInput.getValue(),
    ),
  )

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }
}
