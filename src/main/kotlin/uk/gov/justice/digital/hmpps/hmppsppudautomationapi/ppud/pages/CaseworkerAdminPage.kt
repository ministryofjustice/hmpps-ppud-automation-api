package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import java.time.Duration

@Component
internal class CaseworkerAdminPage(
  private val driver: WebDriver,
  private val pageHelper: PageHelper,
) {

  private val title = "Caseworker Admin"

  @FindBy(id = "btnSearch")
  private lateinit var searchButton: WebElement

  @FindBy(id = "txtSearchFullName")
  private lateinit var fullNameInput: WebElement

  @FindBy(id = "txtSearchUserName")
  private lateinit var userNameInput: WebElement

  @FindBy(id = "ROLE")
  private lateinit var roleDropdown: WebElement

  private val resultsTable: WebElement?
    get() = driver.findElements(By.id("grdCaseworker")).firstOrNull()

  init {
    PageFactory.initElements(driver, this)
  }

  fun verifyOn() {
    WebDriverWait(driver, Duration.ofSeconds(2))
      .until(ExpectedConditions.titleIs(title))
  }

  fun extractActiveUsersByCriteria(fullName: String?, userName: String?): List<PpudUser> {
    resetPage()
    fullName?.let { pageHelper.enterText(fullNameInput, it) }
    userName?.let { pageHelper.enterText(userNameInput, it) }
    searchButton.click()
    return extractActiveUsersFromResultsTable()
  }

  fun extractActiveUsers(): List<PpudUser> {
    resetPage()
    return extractActiveUsersFromResultsTable()
  }

  private fun extractActiveUsersFromResultsTable(): MutableList<PpudUser> {
    val users: MutableList<PpudUser> = mutableListOf()
    var paginglink: WebElement?
    var usersTableRows: List<WebElement>?

    var nextPageNumber = 2
    do {
      verifyOn()
      //XPath: extract table rows containing 8 table cells, that are not within a table header, and are not deleted ("Delete" hyperlink present)
      usersTableRows = resultsTable?.findElements(By.xpath("tbody/tr[count(.//td) = 8 and not(.//th) and .//td[7]/a[text() = 'Delete']]"))
      usersTableRows?.forEach { userTableRow ->
        users.add(
          PpudUser(
            //XPath: extract specific table cells
            userTableRow.findElements(By.xpath("td[2]"))[0].text,
            userTableRow.findElements(By.xpath("td[4]"))[0].text,
          ),
        )
      }
      paginglink = getPageLink(nextPageNumber) ?: getForwardEllipsisLink()
      paginglink?.click()
      ++nextPageNumber
    } while (paginglink != null)

    return users
  }

  private fun getPageLink(nextPageNum: Number): WebElement? {
    //XPath: find the last table row and from its inner table extract the hyperlink that contains the text $nextPageNum from the body->row->cell
    return resultsTable?.findElements(By.xpath("tbody/tr[last()]/td/table/tbody/tr/td/a[text()='$nextPageNum']"))?.firstOrNull()
  }

  private fun getForwardEllipsisLink(): WebElement? {
    //XPath: find the last table row and from its inner table extract the hyperlink that contains the text "..." from the body->row->last cell
    return resultsTable?.findElements(By.xpath("tbody/tr[last()]/td/table/tbody/tr/td[last()]/a[text()='...']"))?.firstOrNull()
  }

  private fun resetPage() {
    pageHelper.selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")
  }
}
