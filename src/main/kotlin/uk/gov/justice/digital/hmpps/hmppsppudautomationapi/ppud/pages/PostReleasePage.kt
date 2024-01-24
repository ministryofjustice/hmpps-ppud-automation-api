package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.PostRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank

@Component
@RequestScope
internal class PostReleasePage(
  private val driver: WebDriver,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
) {
  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_ddliTYPE_OF_LICENCE")
  private lateinit var licenceTypeDropdown: WebElement

  @FindBy(id = "cntDetails_ddliPROBATION_SERVICE")
  private lateinit var probationServiceDropdown: WebElement

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

  fun updatePostRelease(updatePostReleaseRequest: UpdatePostReleaseRequest) {
    selectDropdownOptionIfNotBlank(
      probationServiceDropdown,
      updatePostReleaseRequest.probationService,
      "probation service",
    )

    saveButton.click()
  }

  fun extractPostReleaseDetails(): PostRelease {
    return PostRelease(
      probationService = Select(probationServiceDropdown).firstSelectedOption.text,
      licenceType = Select(licenceTypeDropdown).firstSelectedOption.text,
    )
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }
}
