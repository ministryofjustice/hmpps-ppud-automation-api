package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var pageHelper: PageHelper

  @BeforeEach
  fun beforeEach() {
    driver = mock(WebDriver::class.java)
    pageHelper = mock(PageHelper::class.java)
    roleDropdown = mock(WebElement::class.java)
    userNameInput = mock(WebElement::class.java)
    fullNameInput = mock(WebElement::class.java)
    searchButton = mock(WebElement::class.java)

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
      val users = emptyList<PpudUser>()

      given(driver.title).willReturn("Caseworker Admin")

      val result = caseworkerAdminPage.extractActiveUsers()

      verify(pageHelper).selectDropdownOptionIfNotBlank(roleDropdown, "Level 1", "role")

      assertEquals(users, result)
    }
  }
}
