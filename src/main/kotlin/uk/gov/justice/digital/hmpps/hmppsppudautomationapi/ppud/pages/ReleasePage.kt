package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class ReleasePage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
) {
  @FindBy(id = "igtxtcntDetails_dteRELEASE_DATE")
  private lateinit var dateOfReleaseInput: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun extractReleaseDetails(): Release {
    return Release(
      dateOfRelease = LocalDate.parse(dateOfReleaseInput.getValue(), dateFormatter),
    )
  }
}
