package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class OffenderPage(
  private val driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
  @Value("\${ppud.url}") private val ppudUrl: String,
) {

  @FindBy(id = "cntDetails_txtCRO_PNC")
  private lateinit var croNumberInput: WebElement

  @FindBy(id = "cntDetails_txtNOMS_ID")
  private lateinit var nomsIdInput: WebElement

  @FindBy(id = "cntDetails_txtFIRST_NAMES")
  private lateinit var firstNamesInput: WebElement

  @FindBy(id = "cntDetails_txtFAMILY_NAME")
  private lateinit var familyNameInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOB")
  private lateinit var dateOfBirthInput: WebElement

  @FindBy(id = "T_ctl00treetvOffender")
  private lateinit var navigationTreeView: WebElement

  init {
    PageFactory.initElements(driver, this)
  }

  fun viewOffenderWithId(offenderId: String) {
    driver.get("$ppudUrl/Offender/PersonalDetails.aspx?data=$offenderId")
  }

  fun navigateToNewRecallFor(sentenceDate: LocalDate, releaseDate: LocalDate) {
    val treeView = TreeView(navigationTreeView)
    treeView
      .expandNodeWithText("Sentences")
      .expandNodeWithTextContaining(sentenceDate.format(dateFormatter))
      .expandNodeWithText("Releases")
      .expandNodeWithTextContaining(releaseDate.format(dateFormatter))
      .expandNodeWithTextContaining("Recalls")
      .findNodeWithTextContaining("New")
      .click()
  }

  fun extractOffenderDetails(): Offender {
    val idMatch = Regex(".+?data=(.+)").find(driver.currentUrl)!!
    val (id) = idMatch.destructured

    return Offender(
      id = id,
      croNumber = croNumberInput.getValue(),
      nomsId = nomsIdInput.getValue(),
      firstNames = firstNamesInput.getValue(),
      familyName = familyNameInput.getValue(),
      dateOfBirth = LocalDate.parse(dateOfBirthInput.getValue(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
    )
  }
}
