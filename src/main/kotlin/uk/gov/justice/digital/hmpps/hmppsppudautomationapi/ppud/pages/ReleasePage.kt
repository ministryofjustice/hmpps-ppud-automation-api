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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.extractId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.waitForDropdownPopulation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.selectDropdownOptionIfNotBlank
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class ReleasePage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
  @Value("\${ppud.release.category}") private val category: String,
  @Value("\${ppud.release.releaseType}") private val releaseType: String,
) {
  @FindBy(id = "cntDetails_PageFooter1_cmdSave")
  private lateinit var saveButton: WebElement

  @FindBy(id = "cntDetails_ddliRELEASED_FROM_CATEGORY_ID")
  private lateinit var categoryDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteRELEASE_DATE")
  private lateinit var dateOfReleaseInput: WebElement

  @FindBy(id = "cntDetails_aceiRELEASED_FROM_AutoCompleteTextBox")
  private lateinit var releasedFromInput: WebElement

  @FindBy(id = "cntDetails_aceiRELEASED_FROM_AutoSelect")
  private lateinit var releasedFromDropdown: WebElement

  @FindBy(id = "cntDetails_ddliRELEASED_UNDER")
  private lateinit var releasedUnderDropdown: WebElement

  @FindBy(id = "cntDetails_ddliRELEASE_TYPE")
  private lateinit var releaseTypeDropdown: WebElement

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun isMatching(releasedFrom: String, releasedUnder: String): Boolean {
    return (
      releasedFrom == releasedFromInput.getValue() &&
        releasedUnder == Select(releasedUnderDropdown).firstSelectedOption.text
      )
  }

  fun updateRelease(createdOrUpdatedRelease: CreateOrUpdateReleaseRequest): String {
    selectDropdownOptionIfNotBlank(categoryDropdown, category, "category")
    selectDropdownOptionIfNotBlank(releaseTypeDropdown, releaseType, "release type")
    saveButton.click()
    return extractId(driver, "release page")
  }

  fun createRelease(createdOrUpdatedRelease: CreateOrUpdateReleaseRequest) {
    releasedFromInput.click()
    releasedFromInput.sendKeys(createdOrUpdatedRelease.releasedFrom)
    selectReleasedFromMatch(createdOrUpdatedRelease.releasedFrom)
  }

  fun extractReleaseDetails(): Release {
    return Release(
      category = Select(categoryDropdown).firstSelectedOption.text,
      dateOfRelease = LocalDate.parse(dateOfReleaseInput.getValue(), dateFormatter),
      releasedFrom = releasedFromInput.getValue(),
      releasedUnder = Select(releasedUnderDropdown).firstSelectedOption.text,
      releaseType = Select(releaseTypeDropdown).firstSelectedOption.text,
    )
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  private fun selectReleasedFromMatch(releasedFrom: String) {
    waitForDropdownPopulation(driver, releasedFromDropdown)
    selectDropdownOptionIfNotBlank(releasedFromDropdown, releasedFrom, "released from")
  }
}
