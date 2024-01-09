package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class ReleasePage(
  driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
) {
  @FindBy(id = "cntDetails_ddliRELEASED_FROM_CATEGORY_ID")
  private lateinit var categoryDropdown: WebElement

  @FindBy(id = "igtxtcntDetails_dteRELEASE_DATE")
  private lateinit var dateOfReleaseInput: WebElement

  @FindBy(id = "cntDetails_aceiRELEASED_FROM_AutoCompleteTextBox")
  private lateinit var releasedFromInput: WebElement

  @FindBy(id = "cntDetails_ddliRELEASED_UNDER")
  private lateinit var releasedUnderDropdown: WebElement

  @FindBy(id = "cntDetails_ddliRELEASE_TYPE")
  private lateinit var releaseTypeDropdown: WebElement

  init {
    PageFactory.initElements(driver, this)
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
}
