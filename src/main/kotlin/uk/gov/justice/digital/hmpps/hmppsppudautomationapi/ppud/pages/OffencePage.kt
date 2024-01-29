package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class OffencePage(
  driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
) {
  @FindBy(id = "igtxtcntDetails_dteINDEX_OFFENCE_DATE")
  private lateinit var dateOfIndexOffenceInput: WebElement

  @FindBy(id = "cntDetails_aceiINDEX_OFFENCE_ID_AutoCompleteTextBox")
  private lateinit var indexOffenceInput: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun extractOffenceDetails(): Offence {
    val dateOfIndexValue = dateOfIndexOffenceInput.getValue()
    return Offence(
      dateOfIndexOffence = if (dateOfIndexValue.isEmpty()) null else LocalDate.parse(dateOfIndexValue, dateFormatter),
      indexOffence = indexOffenceInput.getValue(),
    )
  }
}
