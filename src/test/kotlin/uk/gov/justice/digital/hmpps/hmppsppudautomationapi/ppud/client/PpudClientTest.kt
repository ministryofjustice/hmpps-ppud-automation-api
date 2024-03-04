package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.PpudErrorException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SentencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateSearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var webDriverNavigation: Navigation

  @Mock
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Mock
  private lateinit var loginPage: LoginPage

  @Mock
  private lateinit var errorPage: ErrorPage

  @Mock
  private lateinit var searchPage: SearchPage

  @Mock
  private lateinit var newOffenderPage: NewOffenderPage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var offencePage: OffencePage

  @Mock
  private lateinit var postReleasePage: PostReleasePage

  @Mock
  private lateinit var releasePage: ReleasePage

  @Mock
  private lateinit var recallPage: RecallPage

  @Mock
  private lateinit var sentencePage: SentencePage

  @Mock
  private lateinit var sentencePageFactory: SentencePageFactory

  @Mock
  lateinit var createdOffender: CreatedOffender

  private val ppudUrl = "https://ppud.example.com"

  private val absoluteLogoutUrl = "$ppudUrl/logout.aspx"

  private lateinit var ppudUsername: String

  private lateinit var ppudPassword: String

  private lateinit var ppudAdminUsername: String

  private lateinit var ppudAdminPassword: String

  private lateinit var client: OperationalPpudClient

  @BeforeEach
  fun beforeEach() {
    ppudUsername = randomString("username")
    ppudPassword = randomString("password")
    ppudAdminUsername = randomString("adminUsername")
    ppudAdminPassword = randomString("adminPassword")
    client = OperationalPpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      ppudAdminUsername,
      ppudAdminPassword,
      driver,
      errorPage,
      loginPage,
      searchPage,
      navigationTreeViewComponent,
      newOffenderPage,
      offenderPage,
      offencePage,
      postReleasePage,
      recallPage,
      releasePage,
      sentencePageFactory,
    )

    given(driver.navigate()).willReturn(webDriverNavigation)
  }

  @Test
  fun `given search criteria when search offender is called then log in to PPUD and verify success`() {
    runBlocking {
      client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given search criteria when search offender is called then logout once done`() {
    runBlocking {
      client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)

      val inOrder = inOrder(searchPage, webDriverNavigation)
      then(searchPage).should(inOrder).searchByCroNumber(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given Selenium fails and a PPUD error is shown when an operation fails then attempt to gather any PPUD error info`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(searchPage.searchByCroNumber(any())).willThrow(org.openqa.selenium.NoSuchElementException("Test exception"))
      given(errorPage.isShown()).willReturn(true)
      given(errorPage.extractErrorDetails()).willReturn("Some error details")

      val actual = assertThrows<PpudErrorException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      assertEquals("PPUD has displayed an error. Details are: 'Some error details'", actual.message)
      assertTrue(
        actual.cause?.message?.startsWith("Test exception") == true,
        "Exception.cause was '${actual.cause?.message}'",
      )
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

      val inOrder = inOrder(searchPage, webDriverNavigation)
      then(searchPage).should(inOrder).searchByCroNumber(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given a PPUD failure and logout failure when an operation fails then propagate original exception`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(loginPage.urlPath).willReturn("/login")
      doNothing().`when`(webDriverNavigation).to("$ppudUrl/login")
      given(webDriverNavigation.to(absoluteLogoutUrl)).willThrow(RuntimeException("Should be hidden"))
      given(searchPage.searchByCroNumber(any())).willThrow(AutomationException("Expected test exception"))

      val actual = assertThrows<AutomationException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      assertEquals("Expected test exception", actual.message)
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
      then(searchPage).should(never()).searchByFamilyName(any())
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
      then(searchPage).should(never()).searchByFamilyName(any())
    }
  }

  @Test
  fun `given only family name and date of birth when search offender is called then search is performed using family name and date of birth and familyName alone`() {
    runBlocking {
      val familyName = randomString("name")
      val dateOfBirth = randomDate()

      client.searchForOffender(null, null, familyName, dateOfBirth)

      then(searchPage).should(never()).searchByCroNumber(any())
      then(searchPage).should(never()).searchByNomsId(any())
      then(searchPage).should().searchByPersonalDetails(familyName, dateOfBirth)
      then(searchPage).should().searchByFamilyName(familyName)
    }
  }

  @Test
  fun `given search criteria that returns no results when search offender is called then search on cro, noms, personal details and family name`() {
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
      then(searchPage).should(inOrder).searchByFamilyName(familyName)
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

      then(webDriverNavigation).should().to(searchResultLink)
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

      then(webDriverNavigation).should().to(searchResultLink)
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

      val inOrder = inOrder(webDriverNavigation, offenderPage)
      then(webDriverNavigation).should(inOrder).to(searchResultLinks[0])
      then(offenderPage).should(inOrder).extractSearchResultOffenderDetails()
      then(webDriverNavigation).should(inOrder).to(searchResultLinks[1])
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
  fun `given ID when retrieveOffender is called then log in to PPUD and verify success`() {
    runBlocking {
      client.retrieveOffender(randomPpudId())

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given ID when retrieveOffender is called then log out once done`() {
    runBlocking {
      client.retrieveOffender(randomPpudId())

      val inOrder = inOrder(offenderPage, webDriverNavigation)
      then(offenderPage).should(inOrder).extractOffenderDetails(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
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

      val inOrder = inOrder(webDriverNavigation, offenderPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(offenderPage).should(inOrder).extractOffenderDetails(any())
      assertEquals(offender, result)
    }
  }

  @Test
  fun `given offender data when create offender is called then log in to PPUD and verify success`() {
    runBlocking {
      val createOffenderRequest = generateCreateOffenderRequest()
      client.createOffender(createOffenderRequest)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given offender data when create offender is called then log out once done`() {
    runBlocking {
      val createOffenderRequest = generateCreateOffenderRequest()
      client.createOffender(createOffenderRequest)

      val inOrder = inOrder(newOffenderPage, webDriverNavigation)
      then(newOffenderPage).should(inOrder).createOffender(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender data when create offender is called then create offender`() {
    runBlocking {
      given(offenderPage.extractCreatedOffenderDetails(any())).willReturn(createdOffender)
      val createOffenderRequest = generateCreateOffenderRequest()

      client.createOffender(createOffenderRequest)

      val inOrder = inOrder(newOffenderPage, searchPage, offenderPage)
      then(searchPage).should(inOrder).navigateToNewOffender()
      then(newOffenderPage).should(inOrder).verifyOn()
      then(newOffenderPage).should(inOrder).createOffender(createOffenderRequest)
      then(newOffenderPage).should(inOrder).throwIfInvalid()
      then(offenderPage).should(inOrder).updateAdditionalAddresses(any())
      then(offenderPage).should(inOrder).throwIfInvalid()
    }
  }

  @Suppress("UNCHECKED_CAST")
  @Test
  fun `given offender data when create offender is called then return created offender details`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val linkToSentence = "/link/to/sentence"
      val createdOffender = CreatedOffender(id = offenderId, sentence = CreatedSentence(id = sentenceId))
      given(offenderPage.extractCreatedOffenderDetails(any()))
        .will {
          ((it.arguments.first()) as (String) -> CreatedSentence).invoke(linkToSentence)
          createdOffender
        }
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)
      val createOffenderRequest = generateCreateOffenderRequest()

      val newOffender = client.createOffender(createOffenderRequest)

      val inOrder = inOrder(searchPage, offenderPage, webDriverNavigation, sentencePage)
      then(offenderPage).should(inOrder).throwIfInvalid()
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$linkToSentence")
      then(sentencePage).should(inOrder).extractCreatedSentenceDetails()
      assertEquals(offenderId, newOffender.id)
      assertEquals(sentenceId, newOffender.sentence.id)
    }
  }

  @Test
  fun `given offender id and data when update offender is called then log in to PPUD and verify success`() {
    runBlocking {
      val offenderId = randomPpudId()
      val updateOffenderRequest = generateUpdateOffenderRequest()

      client.updateOffender(offenderId, updateOffenderRequest)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given offender id and data when update offender is called then log out once done`() {
    runBlocking {
      val offenderId = randomPpudId()
      val updateOffenderRequest = generateUpdateOffenderRequest()

      client.updateOffender(offenderId, updateOffenderRequest)

      val inOrder = inOrder(offenderPage, webDriverNavigation)
      then(offenderPage).should(inOrder).updateOffender(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender id and data when update offender is called then update offender`() {
    runBlocking {
      val offenderId = randomPpudId()
      val updateOffenderRequest = generateUpdateOffenderRequest()

      client.updateOffender(offenderId, updateOffenderRequest)

      val inOrder = inOrder(offenderPage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(offenderPage).should(inOrder).updateOffender(updateOffenderRequest)
      then(offenderPage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given sentence data when create sentence is called then log in to PPUD and verify success`() {
    runBlocking {
      val offenderId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      client.createSentence(offenderId, request)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given sentence data when create sentence is called then log out once done`() {
    runBlocking {
      val offenderId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      client.createSentence(offenderId, request)

      val inOrder = inOrder(sentencePage, webDriverNavigation)
      then(sentencePage).should(inOrder).createSentence(request)
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given sentence data when create sentence is called then create sentence and return ID`() {
    runBlocking {
      val offenderId = randomPpudId()
      val custodyType = randomString("custodyType")
      val request = generateCreateOrUpdateSentenceRequest(
        custodyType = custodyType,
      )
      val sentenceId = randomPpudId()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)
      given(sentencePage.extractCreatedSentenceDetails()).willReturn(CreatedSentence(sentenceId))

      val newSentence = client.createSentence(offenderId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).navigateToNewSentence()
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).selectCustodyType(custodyType)
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).createSentence(request)
      then(sentencePage).should(inOrder).throwIfInvalid()
      then(sentencePage).should(inOrder).extractCreatedSentenceDetails()
      assertEquals(sentenceId, newSentence.id)
    }
  }

  @Test
  fun `given duplicate sentence data when create sentence is called then do not create sentence and return existing ID`() {
    runBlocking {
      val offenderId = randomPpudId()
      val dateOfSentence = randomDate()
      val custodyType = randomString("custodyType")
      val request = generateCreateOrUpdateSentenceRequest(
        dateOfSentence = dateOfSentence,
        custodyType = custodyType,
      )
      val sentenceId = randomPpudId()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)
      given(navigationTreeViewComponent.extractSentenceLinks(dateOfSentence, custodyType)).willReturn(listOf("/link"))
      given(sentencePage.isMatching(request)).willReturn(true)
      given(sentencePage.extractCreatedSentenceDetails()).willReturn(CreatedSentence(sentenceId))

      val returnedSentence = client.createSentence(offenderId, request)

      val inOrder =
        inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractSentenceLinks(dateOfSentence, custodyType)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/link")
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).isMatching(request)
      then(sentencePage).should(inOrder).extractCreatedSentenceDetails()
      then(navigationTreeViewComponent).should(never()).navigateToNewSentence()
      then(sentencePage).should(never()).createSentence(request)
      then(sentencePage).should(never()).throwIfInvalid()
      assertEquals(sentenceId, returnedSentence.id)
    }
  }

  @Test
  fun `given offender ID and sentence ID and sentence data when update sentence is called then log in to PPUD and verify success`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      client.updateSentence(offenderId, sentenceId, request)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given offender ID and sentence ID and sentence data when update sentence is called then log out once done`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      client.updateSentence(offenderId, sentenceId, request)

      val inOrder = inOrder(sentencePage, webDriverNavigation)
      then(sentencePage).should(inOrder).updateSentence(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender ID and sentence ID and sentence data when update sentence is called then update sentence`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      client.updateSentence(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).navigateToSentenceFor(sentenceId)
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).updateSentence(request)
      then(sentencePage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given offender ID and sentence ID and offence data when update offence is called then log in to PPUD and verify success`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateUpdateOffenceRequest()

      client.updateOffence(offenderId, sentenceId, request)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given offender ID and sentence ID and offence data when update offence is called then log out once done`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateUpdateOffenceRequest()

      client.updateOffence(offenderId, sentenceId, request)

      val inOrder = inOrder(offencePage, webDriverNavigation)
      then(offencePage).should(inOrder).updateOffence(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender ID and sentence ID and offence data when update offence is called then update offence`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateUpdateOffenceRequest()

      client.updateOffence(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, offencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).navigateToOffenceFor(sentenceId)
      then(offencePage).should(inOrder).updateOffence(request)
    }
  }

  @Test
  fun `given offenderID and sentence ID and release data when create or update release is called then log in to PPUD and verify success`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateReleaseRequest()
      given(releasePage.extractReleaseId()).willReturn(randomPpudId())

      client.createOrUpdateRelease(offenderId, sentenceId, request)

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given offender ID and sentence ID and release data when create or update release is called then log out once done`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateReleaseRequest()
      given(releasePage.extractReleaseId()).willReturn(randomPpudId())

      client.createOrUpdateRelease(offenderId, sentenceId, request)

      val inOrder = inOrder(releasePage, webDriverNavigation)
      then(releasePage).should(inOrder).throwIfInvalid()
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender ID and sentence ID and release data for matching release when create or update release is called then update matching release`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val releasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, releasedUnder)
      val matchingReleaseLink = "/link/to/matching/release"
      val releaseId = randomPpudId()
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)).willReturn(
        listOf(
          matchingReleaseLink,
        ),
      )
      given(releasePage.isMatching(releasedFrom, releasedUnder)).willReturn(true)
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      client.createOrUpdateRelease(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$matchingReleaseLink")
      then(releasePage).should(inOrder).updateRelease()
      then(releasePage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given offender ID and sentence ID and release data for new release when create or update release is called then create new release`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val releasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, releasedUnder)
      val releaseId = randomPpudId()
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)).willReturn(listOf())
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      client.createOrUpdateRelease(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(releasePage).should(inOrder).createRelease(request)
      then(releasePage).should(inOrder).throwIfInvalid()
      then(releasePage).should(never()).isMatching(any(), any())
    }
  }

  @Test
  fun `given offender ID and sentence ID and release data for new release when create or update release is called then return ID from persisted release`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val releasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, releasedUnder)
      val releaseId = randomPpudId()
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease))
        .willReturn(listOf()) // Before creation
        .willReturn(listOf("/link/to/persisted/release")) // After creation
      given(releasePage.isMatching(releasedFrom, releasedUnder)).willReturn(true) // After creation
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      val updatedRelease = client.createOrUpdateRelease(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(releasePage).should(inOrder).throwIfInvalid()
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/link/to/persisted/release")
      then(releasePage).should(inOrder).isMatching(releasedFrom, releasedUnder)
      then(releasePage).should(inOrder).extractReleaseId()
      assertEquals(releaseId, updatedRelease.id)
    }
  }

  @Test
  fun `given recall data when create recall is called then log in to PPUD and verify success`() {
    runBlocking {
      val createRecallRequest = generateCreateRecallRequest()
      client.createRecall(
        offenderId = randomPpudId(),
        releaseId = randomPpudId(),
        recallRequest = createRecallRequest,
      )

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given recall data when create recall is called then log out once done`() {
    runBlocking {
      val createRecallRequest = generateCreateRecallRequest()
      client.createRecall(
        offenderId = randomPpudId(),
        releaseId = randomPpudId(),
        recallRequest = createRecallRequest,
      )

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(recallPage).should(inOrder).createRecall(any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given offender ID and sentence ID and release ID and recall data for matching recall when create recall is called then just return ID`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val receivedDateTime = LocalDateTime.now()
      val recommendedTo = PpudUser(randomString("fullName"), randomString("teamName"))
      val createRecallRequest = generateCreateRecallRequest(
        receivedDateTime = receivedDateTime,
        recommendedTo = recommendedTo,
      )
      val matchingRecallLink = "/link/to/matching/recall"
      val recallId = randomPpudId()
      given(navigationTreeViewComponent.extractRecallLinks(releaseId)).willReturn(
        listOf(
          matchingRecallLink,
        ),
      )
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))
      given(recallPage.isMatching(receivedDateTime, recommendedTo)).willReturn(true)

      val returnedRecall = client.createRecall(offenderId, releaseId, createRecallRequest)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, recallPage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$matchingRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(navigationTreeViewComponent).should(never()).navigateToNewRecallFor(any())
      then(recallPage).should(never()).createRecall(any())
      then(recallPage).should(never()).addDetailsMinute(any())
      then(recallPage).should(never()).addContrabandMinuteIfNeeded(createRecallRequest)
      assertEquals(recallId, returnedRecall.id)
    }
  }

  @Test
  fun `given offender ID and sentence ID and release ID and recall data for new recall when create recall is called then create recall`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val receivedDateTime = LocalDateTime.now()
      val recommendedTo = generatePpudUser()
      val createRecallRequest = generateCreateRecallRequest(
        receivedDateTime = receivedDateTime,
        recommendedTo = recommendedTo,
      )
      val nonMatchingRecallLink = "/link/to/non-matching/recall"
      val recallId = randomPpudId()
      given(navigationTreeViewComponent.extractRecallLinks(releaseId)).willReturn(
        listOf(
          nonMatchingRecallLink,
        ),
      )
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))
      given(recallPage.isMatching(receivedDateTime, recommendedTo)).willReturn(false)

      client.createRecall(offenderId, releaseId, createRecallRequest)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, recallPage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$nonMatchingRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(navigationTreeViewComponent).should(inOrder).navigateToNewRecallFor(releaseId)
      then(recallPage).should(inOrder).createRecall(any())
      then(recallPage).should(inOrder).throwIfInvalid()
      then(recallPage).should(inOrder).addDetailsMinute(any())
      then(recallPage).should(inOrder).addContrabandMinuteIfNeeded(createRecallRequest)
    }
  }

  @Test
  fun `given offender ID and sentence ID and release ID and recall data for new recall when create recall is called then return ID from persisted recall`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val receivedDateTime = LocalDateTime.now()
      val recommendedTo = generatePpudUser()
      val createRecallRequest = generateCreateRecallRequest(
        receivedDateTime = receivedDateTime,
        recommendedTo = recommendedTo,
      )
      val nonMatchingRecallLink = "/link/to/non-matching/recall"
      val persistedRecallLink = "/link/to/persisted/recall"
      val recallId = randomPpudId()
      given(navigationTreeViewComponent.extractRecallLinks(releaseId))
        .willReturn(listOf(nonMatchingRecallLink)) // Before creation
        .willReturn(listOf(nonMatchingRecallLink, persistedRecallLink)) // After creation
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))
      given(recallPage.isMatching(receivedDateTime, recommendedTo))
        .willReturn(false) // Before creation
        .willReturn(false) // After creation non-matching
        .willReturn(true) // After creation matching

      val createdRecall = client.createRecall(offenderId, releaseId, createRecallRequest)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, recallPage)
      then(recallPage).should(inOrder).throwIfInvalid()
      then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$nonMatchingRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$persistedRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(recallPage).should(inOrder).extractCreatedRecallDetails()
      assertEquals(recallId, createdRecall.id)
    }
  }

  @Test
  fun `given contraband risk detail when create recall is called then add contraband minute`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest(riskOfContrabandDetails = randomString("contraband"))
      val recallId = randomPpudId()
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))

      client.createRecall(offenderId, releaseId, createRecallRequest)

      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).addDetailsMinute(createRecallRequest)
      then(recallPage).should(inOrder).addContrabandMinuteIfNeeded(createRecallRequest)
    }
  }

  @Test
  fun `given data that PPUD considers invalid when create recall is called then bubble exception`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest()
      val exceptionMessage = randomString("test-exception")
      val exception = RuntimeException(exceptionMessage)
      given(recallPage.throwIfInvalid()).willThrow(exception)

      val actual = assertThrows<RuntimeException> {
        client.createRecall(offenderId, releaseId, createRecallRequest)
      }
      assertEquals(exceptionMessage, actual.message)
      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).createRecall(createRecallRequest)
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given ID when retrieveRecall is called then log in to PPUD and verify success`() {
    runBlocking {
      client.retrieveRecall(randomPpudId())

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given ID when retrieveRecall is called then log out once done`() {
    runBlocking {
      client.retrieveRecall(randomPpudId())

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(recallPage).should(inOrder).extractRecallDetails()
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
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

      val inOrder = inOrder(webDriverNavigation, recallPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl$urlForId")
      then(recallPage).should(inOrder).extractRecallDetails()
      assertEquals(recall, result)
    }
  }

  private fun assertThatLogsOnAndVerifiesSuccess() {
    val inOrder = inOrder(loginPage, searchPage)
    then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
    then(loginPage).should(inOrder).throwIfInvalid()
    then(searchPage).should(inOrder).verifyOn()
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
