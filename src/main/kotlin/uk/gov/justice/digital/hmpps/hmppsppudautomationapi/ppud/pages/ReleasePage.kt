package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import java.time.format.DateTimeFormatter

@Component
internal class ReleasePage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
  private val dateFormatter: DateTimeFormatter,
  @Value("\${ppud.release.category}") private val category: String,
  @Value("\${ppud.release.releaseType}") private val releaseType: String,
) {
  private val pageDescription = "release page"

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

  fun createRelease(createdOrUpdatedRelease: CreateOrUpdateReleaseRequest) {
    with(createdOrUpdatedRelease) {
      // Complete first as additional processing is triggered
      releasedFromInput.click()
      releasedFromInput.sendKeys(this.releasedFrom)

      // Complete standalone fields
      dateOfReleaseInput.click()
      dateOfReleaseInput.sendKeys(this.dateOfRelease.format(dateFormatter))
      pageHelper.selectDropdownOptionIfNotBlank(releasedUnderDropdown, this.releasedUnder, "released under")
      completeNonKeyFields()

      // Complete fields that have been updated/refreshed.
      pageHelper.selectDropdownOptionIfNotBlank(releasedFromDropdown, this.releasedFrom, "released from")

      saveButton.click()
    }
  }

  fun updateRelease() {
    completeNonKeyFields()
    saveButton.click()
  }

  fun extractReleaseId(): String {
    return pageHelper.extractId(pageDescription)
  }

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  private fun completeNonKeyFields() {
    pageHelper.selectDropdownOptionIfNotBlank(categoryDropdown, category, "category")
    pageHelper.selectDropdownOptionIfNotBlank(releaseTypeDropdown, releaseType, "release type")
  }
}
