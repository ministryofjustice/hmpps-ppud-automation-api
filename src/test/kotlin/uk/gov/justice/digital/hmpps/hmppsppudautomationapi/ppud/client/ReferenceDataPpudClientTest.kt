package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomLookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class ReferenceDataPpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var webDriverNavigation: Navigation

  @Mock
  private lateinit var loginPage: LoginPage

  @Mock
  private lateinit var adminPage: AdminPage

  @Mock
  private lateinit var errorPage: ErrorPage

  @Mock
  private lateinit var editLookupsPage: EditLookupsPage

  @Mock
  private lateinit var searchPage: SearchPage

  private val ppudUrl = "https://ppud.example.com"

  private val absoluteLogoutUrl = "$ppudUrl/logout.aspx"

  private lateinit var ppudUsername: String

  private lateinit var ppudPassword: String

  private lateinit var ppudAdminUsername: String

  private lateinit var ppudAdminPassword: String

  private val valueToExclude: String = "Value to Exclude"

  private lateinit var client: ReferenceDataPpudClient

  @BeforeEach
  fun beforeEach() {
    ppudUsername = randomString("username")
    ppudPassword = randomString("password")
    ppudAdminUsername = randomString("adminUsername")
    ppudAdminPassword = randomString("adminPassword")
    client = ReferenceDataPpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      ppudAdminUsername,
      ppudAdminPassword,
      driver,
      errorPage,
      loginPage,
      searchPage,
      valueToExclude,
      adminPage,
      editLookupsPage,
    )

    given(driver.navigate()).willReturn(webDriverNavigation)
  }

  @Test
  fun `given any lookup when retrieveLookupValues is called then log in as admin`() {
    runBlocking {
      val lookupName = randomLookupName()
      given(loginPage.urlPath).willReturn("/login")

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(webDriverNavigation, loginPage, searchPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(loginPage).should(inOrder).login(ppudAdminUsername, ppudAdminPassword)
      then(loginPage).should(inOrder).throwIfInvalid()
      then(searchPage).should(inOrder).verifyOn()
    }
  }

  @Test
  fun `given lookup is not Genders when retrieveLookupValues is called then logout once done`() {
    runBlocking {
      val lookupName = randomLookupName(exclude = listOf(LookupName.Genders))

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(editLookupsPage, webDriverNavigation)
      then(editLookupsPage).should(inOrder).extractLookupValues(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given lookup is Genders when retrieveLookupValues is called then logout once done`() {
    runBlocking {
      val lookupName = LookupName.Genders

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(searchPage, webDriverNavigation)
      then(searchPage).should(inOrder).genderValues()
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given lookup is not Genders when retrieveLookupValues is called then navigate to edit lookups and extract values`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      val lookupName = randomLookupName(exclude = listOf(LookupName.Genders))
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      given(editLookupsPage.extractLookupValues(lookupName)).willReturn(values)

      val result = client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(webDriverNavigation, adminPage, editLookupsPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditLookups()
      then(editLookupsPage).should(inOrder).extractLookupValues(lookupName)
      assertEquals(values, result)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["Value to Exclude", "value to exclude"])
  fun `given lookup is not Genders when retrieveLookupValues is called then exclude configured value from results`(
    valueToExclude: String,
  ) {
    runBlocking {
      val values = listOf(randomString(), randomString(), valueToExclude, randomString())
      val lookupName = randomLookupName(exclude = listOf(LookupName.Genders))
      given(editLookupsPage.extractLookupValues(lookupName)).willReturn(values)

      val result = client.retrieveLookupValues(lookupName)

      assertEquals(values.count() - 1, result.count())
      assertFalse(result.contains(valueToExclude))
    }
  }

  @Test
  fun `given lookup is Genders when retrieveLookupValues is called then navigate to search page and extract values`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      val lookupName = LookupName.Genders
      given(loginPage.urlPath).willReturn("/login")
      given(searchPage.genderValues()).willReturn(values)

      val result = client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(webDriverNavigation, searchPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(searchPage).should(inOrder).verifyOn()
      then(searchPage).should(inOrder).genderValues()
      assertEquals(values, result)
    }
  }
}
