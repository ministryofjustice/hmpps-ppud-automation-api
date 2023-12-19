package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
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
  private lateinit var croOtherNumberInput: WebElement

  @FindBy(id = "igtxtcntDetails_dteDOB")
  private lateinit var dateOfBirthInput: WebElement

  @FindBy(id = "cntDetails_ddliETHNICITY")
  private lateinit var ethnicityDropdown: WebElement

  @FindBy(id = "cntDetails_txtFIRST_NAMES")
  private lateinit var firstNamesInput: WebElement

  @FindBy(id = "cntDetails_txtFAMILY_NAME")
  private lateinit var familyNameInput: WebElement

  @FindBy(id = "cntDetails_ddlsGENDER")
  private lateinit var genderDropdown: WebElement

  @FindBy(id = "T_ctl00treetvOffender")
  private lateinit var navigationTreeView: WebElement

  @FindBy(id = "cntDetails_txtNOMS_ID")
  private lateinit var nomsIdInput: WebElement

  @FindBy(id = "cntDetails_txtPRISON_NUMBER")
  private lateinit var prisonNumberInput: WebElement

  @FindBy(id = "cntDetails_ddliYOUNG_OFFENDER")
  private lateinit var youngOffenderDropdown: WebElement

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

  fun extractCreatedOffenderDetails(): CreatedOffender {
    return CreatedOffender(
      id = extractId(),
    )
  }

  fun extractSearchResultOffenderDetails(): SearchResultOffender {
    return SearchResultOffender(
      id = extractId(),
      croNumber = croOtherNumberInput.getValue(),
      croOtherNumber = croOtherNumberInput.getValue(),
      nomsId = nomsIdInput.getValue(),
      firstNames = firstNamesInput.getValue(),
      familyName = familyNameInput.getValue(),
      dateOfBirth = LocalDate.parse(dateOfBirthInput.getValue(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
    )
  }

  fun extractOffenderDetails(sentenceExtractor: (List<String>) -> List<Sentence>): Offender {
    return Offender(
      id = extractId(),
      croOtherNumber = croOtherNumberInput.getValue(),
      dateOfBirth = LocalDate.parse(dateOfBirthInput.getValue(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      ethnicity = Select(ethnicityDropdown).firstSelectedOption.text,
      familyName = familyNameInput.getValue(),
      firstNames = firstNamesInput.getValue(),
      gender = Select(genderDropdown).firstSelectedOption.text,
      nomsId = nomsIdInput.getValue(),
      prisonNumber = prisonNumberInput.getValue(),
      youngOffender = Select(youngOffenderDropdown).firstSelectedOption.text,
      sentences = sentenceExtractor(determineSentenceLinks()),
    )
  }

  private fun determineSentenceLinks(): List<String> {
    return TreeView(navigationTreeView)
      .expandNodeWithText("Sentences")
      .children()
      .filter { it.text.startsWith("New").not() }
      .map { it.getAttribute("igurl") }
  }

  private fun extractId(): String {
    val idMatch = Regex(".+?data=(.+)").find(driver.currentUrl)!!
    val (id) = idMatch.destructured
    return id
  }
}
