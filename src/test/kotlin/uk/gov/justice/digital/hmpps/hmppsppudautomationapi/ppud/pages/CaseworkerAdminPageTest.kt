package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class CaseworkerAdminPageTest {

  @InjectMocks
  private lateinit var caseworkerAdminPage: CaseworkerAdminPage

  @Mock
  private lateinit var roleDropdown: WebElement

  @Mock
  private lateinit var fullNameInput: WebElement

  @Mock
  private lateinit var userNameInput: WebElement

  @Mock
  private lateinit var searchButton: WebElement

  @Mock
  private lateinit var resultsTable: WebElement

  @Mock
  private lateinit var tableRow: WebElement

  @Mock
  private lateinit var fullNameTableCell: WebElement

  @Mock
  private lateinit var teamNameTableCell: WebElement

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var pageHelper: PageHelper

  @BeforeEach
  fun beforeEach() {
    given(driver.findElements(By.id("grdCaseworker"))).willReturn(mutableListOf(resultsTable))

    caseworkerAdminPage = CaseworkerAdminPage(driver, pageHelper)

    ReflectionTestUtils.setField(caseworkerAdminPage, "searchButton", searchButton)
    ReflectionTestUtils.setField(caseworkerAdminPage, "fullNameInput", fullNameInput)
    ReflectionTestUtils.setField(caseworkerAdminPage, "userNameInput", userNameInput)
    ReflectionTestUtils.setField(caseworkerAdminPage, "roleDropdown", roleDropdown)
  }

  @Test
  fun `searches with non-null fullName and userName criteria returns list of users (empty in the case)`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = randomString("userName")
      val users = emptyList<PpudUser>()

      given(driver.title).willReturn("Caseworker Admin")

      val result = caseworkerAdminPage.extractActiveUsersByCriteria(fullName, userName)

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")
      verify(pageHelper).enterText(fullNameInput, fullName)
      verify(pageHelper).enterText(userNameInput, userName)
      verify(searchButton).click()

      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with non-null fullName and null userName criteria returns list of users (empty in the case)`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName: String? = null
      val users = emptyList<PpudUser>()

      given(driver.title).willReturn("Caseworker Admin")

      val result = caseworkerAdminPage.extractActiveUsersByCriteria(fullName, userName)

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")
      verify(pageHelper).enterText(fullNameInput, fullName)
      verify(pageHelper, never()).enterText(eq(userNameInput), any())
      verify(searchButton).click()

      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with null fullName and non-null userName criteria returns list of users (empty in the case)`() {
    runBlocking {
      val fullName: String? = null
      val userName = randomString("userName")
      val users = emptyList<PpudUser>()

      given(driver.title).willReturn("Caseworker Admin")

      val result = caseworkerAdminPage.extractActiveUsersByCriteria(fullName, userName)

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")
      verify(pageHelper, never()).enterText(eq(fullNameInput), any())
      verify(pageHelper).enterText(userNameInput, userName)
      verify(searchButton).click()

      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with null fullName and null userName criteria returns list of users (empty in the case)`() {
    runBlocking {
      val fullName: String? = null
      val userName: String? = null
      val users = emptyList<PpudUser>()

      given(driver.title).willReturn("Caseworker Admin")

      val result = caseworkerAdminPage.extractActiveUsersByCriteria(fullName, userName)

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")
      verify(pageHelper, never()).enterText(eq(fullNameInput), any())
      verify(pageHelper, never()).enterText(eq(userNameInput), any())
      verify(searchButton).click()

      assertEquals(users, result)
    }
  }

  @Test
  fun `extract active users should return list of users`() {
    runBlocking {
      val users = listOf<PpudUser>(PpudUser("FullName", "TeamName"))

      given(driver.title).willReturn("Caseworker Admin")
      //XPath: extract table rows containing 8 table cells, that are not within a table header, and are not deleted ("Delete" hyperlink present)
      given(resultsTable.findElements(By.xpath("tbody/tr[count(.//td) = 8 and not(.//th) and .//td[7]/a[text() = 'Delete']]")))
        .willReturn(listOf(tableRow))
      //XPath: extract specific table cells
      given(tableRow.findElements(By.xpath("td[2]"))).willReturn(listOf(fullNameTableCell))
      given(tableRow.findElements(By.xpath("td[4]"))).willReturn(listOf(teamNameTableCell))
      given(fullNameTableCell.text).willReturn("FullName")
      given(teamNameTableCell.text).willReturn("TeamName")

      val result = caseworkerAdminPage.extractActiveUsers()

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")

      assertEquals(users, result)
    }
  }
}
