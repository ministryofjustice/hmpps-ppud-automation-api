package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
internal class SearchPage(private val driver: WebDriver) {

  private val title = "Search"

  @FindBy(id = "content_txtCROPNC")
  private val croNumberInput: WebElement? = null

  @FindBy(id = "content_txtNomId")
  private val nomsIdInput: WebElement? = null

  @FindBy(id = "content_txtFamilyName")
  private val familyNameInput: WebElement? = null

  @FindBy(id = "igtxtcontent_dtpDOBFrom")
  private val dateOfBirthFromInput: WebElement? = null

  @FindBy(id = "igtxtcontent_dtpDOBTo")
  private val dateOfBirthToInput: WebElement? = null

  @FindBy(id = "content_cmdSearch")
  private val searchButton: WebElement? = null

  @FindBy(id = "content_cmdClear")
  private val clearButton: WebElement? = null

  private val resultsTable: WebElement?
    get() = driver.findElements(By.id("content_gvSearch")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun verifyOn() {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
  }

  fun searchByCroNumber(croNumber: String) {
    clearButton?.click()
    croNumberInput?.sendKeys(croNumber)
    searchButton?.click()
  }

  fun searchByNomsId(nomsId: String) {
    clearButton?.click()
    nomsIdInput?.sendKeys(nomsId)
    searchButton?.click()
  }

  fun searchByPersonalDetails(familyName: String, dateOfBirth: LocalDate) {
    val typingDateOfBirth = dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    clearButton?.click()
    familyNameInput?.sendKeys(familyName)
    dateOfBirthFromInput?.click()
    dateOfBirthFromInput?.sendKeys(typingDateOfBirth)
    dateOfBirthToInput?.click()
    dateOfBirthToInput?.sendKeys(typingDateOfBirth)
    searchButton?.click()
  }

  fun searchResultsCount(): Int {
    val resultsLinks = resultsTable?.findElements(By.linkText("Select"))
    return resultsLinks?.size ?: 0
  }

  fun searchResultsLinks(): List<String> {
    val resultsElements = resultsTable?.findElements(By.linkText("Select")) ?: emptyList<WebElement>()
    return resultsElements.map { it.getAttribute("href") }
  }
}
