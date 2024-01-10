package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.willReturnConsecutively
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateSearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomLookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var navigation: Navigation

  @Mock
  private lateinit var loginPage: LoginPage

  @Mock
  private lateinit var adminPage: AdminPage

  @Mock
  private lateinit var editLookupsPage: EditLookupsPage

  @Mock
  private lateinit var searchPage: SearchPage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var newOffenderPage: NewOffenderPage

  @Mock
  private lateinit var sentencePageFactory: SentencePageFactory

  @Mock
  private lateinit var releasePage: ReleasePage

  @Mock
  private lateinit var recallPage: RecallPage

  private val ppudUrl = "https://ppud.example.com"

  private val absoluteLogoutUrl = "$ppudUrl/logout.aspx"

  private lateinit var ppudUsername: String

  private lateinit var ppudPassword: String

  private lateinit var ppudAdminUsername: String

  private lateinit var ppudAdminPassword: String

  private lateinit var client: PpudClient

  @BeforeEach
  fun beforeEach() {
    ppudUsername = randomString("username")
    ppudPassword = randomString("password")
    ppudAdminUsername = randomString("adminUsername")
    ppudAdminPassword = randomString("adminPassword")
    client = PpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      ppudAdminUsername,
      ppudAdminPassword,
      driver,
      loginPage,
      adminPage,
      editLookupsPage,
      searchPage,
      offenderPage,
      newOffenderPage,
      sentencePageFactory,
      releasePage,
      recallPage,
    )

    given(driver.navigate()).willReturn(navigation)
  }

  @Test
  fun `given search criteria when search offender is called then log in to PPUD and verify we are on search page`() {
    runBlocking {
      client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))

      then(loginPage).should().login(ppudUsername, ppudPassword)
      then(searchPage).should().verifyOn()
    }
  }

  @Test
  fun `given search criteria when search offender is called then logout once done`() {
    runBlocking {
      client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)

      val inOrder = inOrder(searchPage, navigation)
      then(searchPage).should(inOrder).searchByCroNumber(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given a PPUD failure when an operation fails then still attempt to logout`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(searchPage.searchByCroNumber(any())).willThrow(AutomationException("Test exception"))

      assertThrows<AutomationException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }

      val inOrder = inOrder(searchPage, navigation)
      then(searchPage).should(inOrder).searchByCroNumber(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given a PPUD failure and logout failure when an operation fails then propagate original exception`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(loginPage.urlPath).willReturn("/login")
      doNothing().`when`(navigation).to("$ppudUrl/login")
      given(navigation.to(absoluteLogoutUrl)).willThrow(RuntimeException("Should be hidden"))
      given(searchPage.searchByCroNumber(any())).willThrow(AutomationException("Expected Test Exception"))

      val actual = assertThrows<AutomationException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      assertEquals("Expected Test Exception", actual.message)
    }
  }

  @Test
  fun `given only CRO number when search offender is called then search is performed using only croNumber number`() {
    runBlocking {
      val croNumber = randomCroNumber()

      client.searchForOffender(croNumber, null, null, null)

      then(searchPage).should().searchByCroNumber(croNumber)
      then(searchPage).should(never()).searchByNomsId(any())
      then(searchPage).should(never()).searchByPersonalDetails(any(), any())
    }
  }

  @Test
  fun `given only NOMS ID when search offender is called then search is performed using only NOMS ID`() {
    runBlocking {
      val nomsId = randomNomsId()

      client.searchForOffender(null, nomsId, null, null)

      then(searchPage).should(never()).searchByCroNumber(any())
      then(searchPage).should().searchByNomsId(nomsId)
      then(searchPage).should(never()).searchByPersonalDetails(any(), any())
    }
  }

  @Test
  fun `given only family name and date of birth when search offender is called then search is performed using only family name and date of birth`() {
    runBlocking {
      val familyName = randomString("name")
      val dateOfBirth = randomDate()

      client.searchForOffender(null, null, familyName, dateOfBirth)

      then(searchPage).should(never()).searchByCroNumber(any())
      then(searchPage).should(never()).searchByNomsId(any())
      then(searchPage).should().searchByPersonalDetails(familyName, dateOfBirth)
    }
  }

  @Test
  fun `given search criteria that returns no results when search offender is called then search on cro, noms and personal details`() {
    runBlocking {
      val croNumber = randomCroNumber()
      val nomsId = randomNomsId()
      val familyName = randomString("name")
      val dateOfBirth = randomDate()
      given(searchPage.searchResultsCount()).willReturn(0)

      client.searchForOffender(croNumber, nomsId, familyName, dateOfBirth)

      val inOrder = inOrder(searchPage)
      then(searchPage).should(inOrder).searchByCroNumber(croNumber)
      then(searchPage).should(inOrder).searchByNomsId(nomsId)
      then(searchPage).should(inOrder).searchByPersonalDetails(familyName, dateOfBirth)
    }
  }

  @Test
  fun `given CRO Number that returns single result when search offender is called then return offender details`() {
    runBlocking {
      val croNumber = randomCroNumber()
      val searchResultLink = "/link/to/offender/details"
      val offender = generateSearchResultOffender(croOtherNumber = croNumber)
      setUpMocksToReturnSingleSearchResult(searchResultLink, offender)

      val result = client.searchForOffender(croNumber, null, null, null)

      then(navigation).should().to(searchResultLink)
      then(offenderPage).should().extractSearchResultOffenderDetails()
      assertEquals(offender, result.single())
    }
  }

  @Test
  fun `given NOMS ID that returns single result when search offender is called then return offender details`() {
    runBlocking {
      val nomsId = randomNomsId()
      val searchResultLink = "/link/to/offender/details/matching/nomsId"
      val offender = generateSearchResultOffender(nomsId = nomsId)
      setUpMocksToReturnSingleSearchResult(searchResultLink, offender)

      val result = client.searchForOffender(null, nomsId, null, null)

      then(navigation).should().to(searchResultLink)
      then(offenderPage).should().extractSearchResultOffenderDetails()
      assertEquals(offender, result.single())
    }
  }

  @Test
  fun `given family name and date of birth that return multiple results when search offender is called then return offender details`() {
    runBlocking {
      val familyName = randomString("familyName")
      val dateOfBirth = randomDate()
      val searchResultLinks = listOf(
        "/link/to/offender/details/1",
        "/link/to/offender/details/2",
      )
      val offenders = listOf(
        generateSearchResultOffender(familyName = familyName, dateOfBirth = dateOfBirth),
        generateSearchResultOffender(familyName = familyName, dateOfBirth = dateOfBirth),
      )
      setUpMocksToReturnMultipleSearchResults(searchResultLinks, offenders)

      val result = client.searchForOffender(null, null, familyName, dateOfBirth)

      val inOrder = inOrder(navigation, offenderPage)
      then(navigation).should(inOrder).to(searchResultLinks[0])
      then(offenderPage).should(inOrder).extractSearchResultOffenderDetails()
      then(navigation).should(inOrder).to(searchResultLinks[1])
      then(offenderPage).should(inOrder).extractSearchResultOffenderDetails()
      assertEquals(offenders, result)
    }
  }

  @Test
  fun `given search criteria and PPUD login page is failing when search offender is called then exception is bubbled up`() {
    runBlocking {
      given(loginPage.verifyOn()).willThrow(NotFoundException())

      assertThrows<NotFoundException> {
        client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))
      }
    }
  }

  @Test
  fun `given ID when retrieveOffender is called then log in to PPUD and verify we are on search page`() {
    runBlocking {
      client.retrieveOffender(randomPpudId())

      val inOrder = inOrder(loginPage, searchPage)
      then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
      then(searchPage).should(inOrder).verifyOn()
    }
  }

  @Test
  fun `given ID when retrieveOffender is called then log out once done`() {
    runBlocking {
      client.retrieveOffender(randomPpudId())

      val inOrder = inOrder(offenderPage, navigation)
      then(offenderPage).should(inOrder).extractOffenderDetails(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given ID when retrieveOffender is called then navigate to offender and extract details`() {
    runBlocking {
      val offenderId = randomPpudId()
      val offender = generateOffender(id = offenderId)
      given(loginPage.urlPath).willReturn("/login")
      given(offenderPage.extractOffenderDetails(any<(List<String>) -> List<Sentence>>())).willReturn(offender)

      val result = client.retrieveOffender(offenderId)

      val inOrder = inOrder(navigation, offenderPage)
      then(navigation).should(inOrder).to("$ppudUrl/login")
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      assertEquals(offender, result)
    }
  }

  @Test
  fun `given offender data when create offender is called then log in to PPUD and verify we are on search page`() {
    runBlocking {
      val createOffenderRequest = generateCreateOffenderRequest()
      client.createOffender(createOffenderRequest)

      val inOrder = inOrder(loginPage, searchPage)
      then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
      then(searchPage).should(inOrder).verifyOn()
    }
  }

  @Test
  fun `given offender data when create offender is called then log out once done`() {
    runBlocking {
      val createOffenderRequest = generateCreateOffenderRequest()
      client.createOffender(createOffenderRequest)

      val inOrder = inOrder(newOffenderPage, navigation)
      then(newOffenderPage).should(inOrder).createOffender(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender data when create offender is called then create offender and return ID`() {
    runBlocking {
      val offenderId = randomPpudId()
      val createOffenderRequest = generateCreateOffenderRequest()
      given(offenderPage.extractCreatedOffenderDetails()).willReturn(CreatedOffender(offenderId))

      val newOffender = client.createOffender(createOffenderRequest)

      val inOrder = inOrder(newOffenderPage, searchPage)
      then(searchPage).should(inOrder).navigateToNewOffender()
      then(newOffenderPage).should(inOrder).verifyOn()
      then(newOffenderPage).should(inOrder).createOffender(createOffenderRequest)
      then(newOffenderPage).should(inOrder).throwIfInvalid()
      assertEquals(offenderId, newOffender.id)
    }
  }

  @Test
  fun `given recall data when create recall is called then log in to PPUD and verify we are on search page`() {
    runBlocking {
      val createRecallRequest = generateCreateRecallRequest()
      client.createRecall(randomPpudId(), createRecallRequest)

      val inOrder = inOrder(loginPage, searchPage)
      then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
      then(searchPage).should(inOrder).verifyOn()
    }
  }

  @Test
  fun `given recall data when create recall is called then log out once done`() {
    runBlocking {
      val createRecallRequest = generateCreateRecallRequest()
      client.createRecall(randomPpudId(), createRecallRequest)

      val inOrder = inOrder(recallPage, navigation)
      then(recallPage).should(inOrder).createRecall(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given recall data when create recall is called then create recall and return ID`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceDate = randomDate()
      val releaseDate = randomDate()
      val createRecallRequest = generateCreateRecallRequest(
        sentenceDate = sentenceDate,
        releaseDate = releaseDate,
      )
      val recallId = randomPpudId()
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))

      val newRecall = client.createRecall(offenderId, createRecallRequest)

      val inOrder = inOrder(offenderPage, recallPage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(offenderPage).should(inOrder).navigateToNewRecallFor(sentenceDate, releaseDate)
      then(recallPage).should(inOrder).createRecall(createRecallRequest)
      then(recallPage).should(inOrder).addDetailsMinute(createRecallRequest)
      assertEquals(recallId, newRecall.id)
    }
  }

  @Test
  fun `given contraband risk detail when create recall is called then add contraband minute`() {
    runBlocking {
      val offenderId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest(riskOfContrabandDetails = randomString("contraband"))
      val recallId = randomPpudId()
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))

      client.createRecall(offenderId, createRecallRequest)

      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).addDetailsMinute(createRecallRequest)
      then(recallPage).should(inOrder).addContrabandMinuteIfNeeded(createRecallRequest)
    }
  }

  @Test
  fun `given data that PPUD considers invalid when create recall is called then bubble exception`() {
    runBlocking {
      val offenderId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest()
      val exceptionMessage = randomString("test-exception")
      val exception = RuntimeException(exceptionMessage)
      given(recallPage.throwIfInvalid()).willThrow(exception)

      val actual = assertThrows<RuntimeException> {
        client.createRecall(offenderId, createRecallRequest)
      }
      assertEquals(exceptionMessage, actual.message)
      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).createRecall(createRecallRequest)
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given ID when retrieveRecall is called then log in to PPUD and verify we are on search page`() {
    runBlocking {
      client.retrieveRecall(randomPpudId())

      val inOrder = inOrder(loginPage, searchPage)
      then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
      then(searchPage).should(inOrder).verifyOn()
    }
  }

  @Test
  fun `given ID when retrieveRecall is called then log out once done`() {
    runBlocking {
      client.retrieveRecall(randomPpudId())

      val inOrder = inOrder(recallPage, navigation)
      then(recallPage).should(inOrder).extractRecallDetails()
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given ID when retrieveRecall is called then navigate to recall and extract details`() {
    runBlocking {
      val recallId = randomPpudId()
      val recall = generateRecall(id = recallId)
      val urlForId = "/something/recall?data=$recallId"
      given(loginPage.urlPath).willReturn("/login")
      given(recallPage.urlFor(recallId)).willReturn(urlForId)
      given(recallPage.extractRecallDetails()).willReturn(recall)
      val result = client.retrieveRecall(recallId)

      val inOrder = inOrder(navigation, recallPage)
      then(navigation).should(inOrder).to("$ppudUrl/login")
      then(navigation).should(inOrder).to("$ppudUrl$urlForId")
      then(recallPage).should(inOrder).extractRecallDetails()
      assertEquals(recall, result)
    }
  }

  @Test
  fun `given any lookup when retrieveLookupValues is called then log in as admin`() {
    runBlocking {
      val lookupName = randomLookupName()
      given(loginPage.urlPath).willReturn("/login")

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(navigation, loginPage)
      then(navigation).should(inOrder).to("$ppudUrl/login")
      then(loginPage).should(inOrder).login(ppudAdminUsername, ppudAdminPassword)
    }
  }

  @Test
  fun `given lookup is not Genders when retrieveLookupValues is called then logout once done`() {
    runBlocking {
      val lookupName = randomLookupName(exclude = listOf(LookupName.Genders))

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(editLookupsPage, navigation)
      then(editLookupsPage).should(inOrder).extractLookupValues(any())
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given lookup is Genders when retrieveLookupValues is called then logout once done`() {
    runBlocking {
      val lookupName = LookupName.Genders

      client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(searchPage, navigation)
      then(searchPage).should(inOrder).genderValues()
      then(navigation).should(inOrder).to(absoluteLogoutUrl)
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

      val inOrder = inOrder(navigation, adminPage, editLookupsPage)
      then(navigation).should(inOrder).to("$ppudUrl/login")
      then(navigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditLookups()
      then(editLookupsPage).should(inOrder).extractLookupValues(lookupName)
      assertEquals(values, result)
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

      val inOrder = inOrder(navigation, searchPage)
      then(navigation).should(inOrder).to("$ppudUrl/login")
      then(searchPage).should(inOrder).verifyOn()
      then(searchPage).should(inOrder).genderValues()
      assertEquals(values, result)
    }
  }

  private fun setUpMocksToReturnSingleSearchResult(
    searchResultLink: String,
    searchResultOffender: SearchResultOffender,
  ) {
    given(searchPage.searchResultsCount()).willReturn(1)
    given(searchPage.searchResultsLinks()).willReturn(listOf(searchResultLink))
    given(offenderPage.extractSearchResultOffenderDetails()).willReturn(searchResultOffender)
  }

  private fun setUpMocksToReturnMultipleSearchResults(
    searchResultLinks: List<String>,
    searchResultOffenders: List<SearchResultOffender>,
  ) {
    given(searchPage.searchResultsCount()).willReturn(searchResultLinks.size)
    given(searchPage.searchResultsLinks()).willReturn(searchResultLinks)
    given(offenderPage.extractSearchResultOffenderDetails()).willReturnConsecutively(searchResultOffenders)
  }
}
