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
import org.mockito.kotlin.isNull
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import org.mockito.kotlin.willReturnConsecutively
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.PpudErrorException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offence.OffenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.CaseworkerAdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.BaseSentencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateAddMinuteRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateSearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OperationalPpudClientTest {

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
  private lateinit var offencePage: OffencePage

  @Mock
  private lateinit var recallPage: RecallPage

  @Mock
  private lateinit var sentencePage: BaseSentencePage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var adminPage: AdminPage

  @Mock
  private lateinit var caseworkerAdminPage: CaseworkerAdminPage

  @Mock
  private lateinit var offenceClient: OffenceClient

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
      recallPage,
      offenceClient,
      sentencePageFactory,
      adminPage,
      caseworkerAdminPage,
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
  fun `given Selenium fails when an idempotent operation fails then try again`() {
    runBlocking {
      // Use search as an example, but this test applies to any IDEMPOTENT call
      given(searchPage.searchByCroNumber(any())).willThrow(org.openqa.selenium.NoSuchElementException("Test Selenium exception"))

      assertThrows<AutomationException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      then(searchPage).should(times(2)).searchByCroNumber(any())
    }
  }

  @Test
  fun `given Selenium fails when a create offender operation fails then do not try again`() {
    runBlocking {
      // Create offender is not idempotent
      given(newOffenderPage.createOffender(any())).willThrow(org.openqa.selenium.NoSuchElementException("Test Selenium exception"))
      val createOffenderRequest = generateCreateOffenderRequest()

      assertThrows<AutomationException> {
        client.createOffender(createOffenderRequest)
      }
      then(newOffenderPage).should(times(1)).createOffender(any())
    }
  }

  @Test
  fun `given a non-Selenium fail when an operation fails then do not try again`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(searchPage.searchByCroNumber(any())).willThrow(RuntimeException("Test non-Selenium exception"))

      assertThrows<RuntimeException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      then(searchPage).should(times(1)).searchByCroNumber(any())
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
  fun `given Selenium fails and a PPUD error is not shown when an operation fails then wrap exception and include URL`() {
    runBlocking {
      // Use search as an example, but this test applies to any call
      given(searchPage.searchByCroNumber(any())).willThrow(org.openqa.selenium.NoSuchElementException("Test exception"))
      given(errorPage.isShown()).willReturn(false)
      given(driver.currentUrl).willReturn("/current/url")

      val actual = assertThrows<AutomationException> {
        client.searchForOffender(croNumber = "cro", nomsId = null, familyName = null, dateOfBirth = null)
      }
      assertEquals("Exception occurred when performing PPUD operation. Current URL is '/current/url'", actual.message)
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
  fun `given search matches invalid offender when search offender is called then return results with invalid offender excluded`() {
    // This test is because we encountered an offender in PPUD Internal Test that did not render correctly.
    // This meant that the date of birth was empty and could not be parsed, causing an exception.
    // This looks like some dodgy test data and would hopefully never occur in production, but we handle it to avoid
    // similar failures in the test environments.
    runBlocking {
      val familyName = randomString("familyName")
      val dateOfBirth = randomDate()
      val searchResultLinks = listOf(
        "/link/to/offender/details/invalid",
        "/link/to/offender/details/1",
      )
      val offenders = listOf(
        generateSearchResultOffender(familyName = familyName, dateOfBirth = dateOfBirth),
      )
      setUpMocksToReturnSearchResultsWithInvalidOffender(searchResultLinks, offenders)

      val result = client.searchForOffender(null, null, familyName, dateOfBirth)

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
      then(offenderPage).should(inOrder).verifyOn()
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
      then(offencePage).should(inOrder).verifyOn()
      then(offencePage).should(inOrder).updateOffence(request)
    }
  }

  @Test
  fun `given recall ID and document data when upload mandatory document is called then log in to PPUD and verify success`() {
    runBlocking {
      val uploadMandatoryDocumentRequest = generateUploadMandatoryDocumentRequest()
      client.uploadMandatoryDocument(
        recallId = randomPpudId(),
        uploadMandatoryDocumentRequest = uploadMandatoryDocumentRequest,
        filepath = randomString("path"),
      )

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given recall ID and document data when upload mandatory document is called then log out once done`() {
    runBlocking {
      val uploadMandatoryDocumentRequest = generateUploadMandatoryDocumentRequest()
      client.uploadMandatoryDocument(
        recallId = randomPpudId(),
        uploadMandatoryDocumentRequest = uploadMandatoryDocumentRequest,
        filepath = randomString("path"),
      )

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(recallPage).should(inOrder).uploadMandatoryDocument(any(), any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given recall ID and document data when upload mandatory document is called then navigate to recall and upload document`() {
    runBlocking {
      val recallId = randomPpudId()
      val uploadMandatoryDocumentRequest = generateUploadMandatoryDocumentRequest()
      val filepath = randomString("path")
      val url = randomString("/url")
      val absoluteUrl = ppudUrl + url
      given(recallPage.urlFor(recallId)).willReturn(url)

      client.uploadMandatoryDocument(recallId, uploadMandatoryDocumentRequest, filepath)

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(webDriverNavigation).should(inOrder).to(absoluteUrl)
      then(recallPage).should(inOrder).uploadMandatoryDocument(uploadMandatoryDocumentRequest, filepath)
      then(recallPage).should(inOrder).markMandatoryDocumentAsReceived(uploadMandatoryDocumentRequest.category)
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given recall ID and document data when upload additional document is called then log in to PPUD and verify success`() {
    runBlocking {
      val uploadAdditionalDocumentRequest = generateUploadAdditionalDocumentRequest()
      client.uploadAdditionalDocument(
        recallId = randomPpudId(),
        uploadAdditionalDocumentRequest = uploadAdditionalDocumentRequest,
        filepath = randomString("path"),
      )

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given recall ID and document data when upload additional document is called then log out once done`() {
    runBlocking {
      val uploadAdditionalDocumentRequest = generateUploadAdditionalDocumentRequest()
      client.uploadAdditionalDocument(
        recallId = randomPpudId(),
        uploadAdditionalDocumentRequest = uploadAdditionalDocumentRequest,
        filepath = randomString("path"),
      )

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(recallPage).should(inOrder).uploadAdditionalDocument(any(), any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given recall ID and document data when upload additional document is called then navigate to recall and upload document`() {
    runBlocking {
      val recallId = randomPpudId()
      val uploadAdditionalDocumentRequest = generateUploadAdditionalDocumentRequest()
      val filepath = randomString("path")
      val url = randomString("/url")
      val absoluteUrl = ppudUrl + url
      given(recallPage.urlFor(recallId)).willReturn(url)

      client.uploadAdditionalDocument(recallId, uploadAdditionalDocumentRequest, filepath)

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(webDriverNavigation).should(inOrder).to(absoluteUrl)
      then(recallPage).should(inOrder).uploadAdditionalDocument(uploadAdditionalDocumentRequest, filepath)
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given recall ID and minute data when add minute is called then log in to PPUD and verify success`() {
    runBlocking {
      val request = generateAddMinuteRequest()
      client.addMinute(
        recallId = randomPpudId(),
        request = request,
      )

      assertThatLogsOnAndVerifiesSuccess()
    }
  }

  @Test
  fun `given recall ID and minute data when add minute is called then log out once done`() {
    runBlocking {
      val request = generateAddMinuteRequest()
      client.addMinute(
        recallId = randomPpudId(),
        request = request,
      )

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(recallPage).should(inOrder).addMinute(any(), any())
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given recall ID and minute data when add minute is called then navigate to recall and add minute`() {
    runBlocking {
      val recallId = randomPpudId()
      val request = generateAddMinuteRequest()
      val url = randomString("/url")
      val absoluteUrl = ppudUrl + url
      given(recallPage.urlFor(recallId)).willReturn(url)
      given(recallPage.hasMatchingMinute(request.subject, request.text)).willReturn(false)

      client.addMinute(recallId, request)

      val inOrder = inOrder(recallPage, webDriverNavigation)
      then(webDriverNavigation).should(inOrder).to(absoluteUrl)
      then(recallPage).should(inOrder).addMinute(request.subject, request.text)
    }
  }

  @Test
  fun `given recall ID and duplicated minute data when add minute is called then do not create minute`() {
    runBlocking {
      val recallId = randomPpudId()
      val request = generateAddMinuteRequest()
      val url = randomString("/url")
      given(recallPage.urlFor(recallId)).willReturn(url)
      given(recallPage.hasMatchingMinute(request.subject, request.text)).willReturn(true)

      client.addMinute(recallId, request)

      then(recallPage).should(never()).addMinute(any(), any())
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

  @Test
  fun `given search criteria when search user is called then log in to PPUD and verify success`() {
    runBlocking {
      client.searchActiveUsers("Joe Bloggs", null)

      assertThatLogsOnAndVerifiesSuccess(asAdmin = true)
    }
  }

  @Test
  fun `given search criteria when search user is called then logout once done`() {
    runBlocking {
      client.searchActiveUsers("Joe Bloggs", null)

      val inOrder = inOrder(adminPage, webDriverNavigation)
      then(adminPage).should(inOrder).goToEditCaseworker()
      then(webDriverNavigation).should(inOrder).to(absoluteLogoutUrl)
    }
  }

  @Test
  fun `given user fullName and userName that return multiple results when searchUsers is called then return user's details`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = randomString("userName")
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(caseworkerAdminPage.extractActiveUsersByCriteria(any(), any())).willReturn(users)

      val result = client.searchActiveUsers(fullName, userName)

      val inOrder = inOrder(webDriverNavigation, adminPage, caseworkerAdminPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditCaseworker()
      then(caseworkerAdminPage).should(inOrder).extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `given user fullName and null userName that return multiple results when searchUsers is called then return user's details`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = null
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(caseworkerAdminPage.extractActiveUsersByCriteria(any(), isNull())).willReturn(users)

      val result = client.searchActiveUsers(fullName, userName)

      val inOrder = inOrder(webDriverNavigation, adminPage, caseworkerAdminPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditCaseworker()
      then(caseworkerAdminPage).should(inOrder).extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `given null user fullName and userName that return multiple results when searchUsers is called then return user's details`() {
    runBlocking {
      val fullName = null
      val userName = randomString("userName")
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(caseworkerAdminPage.extractActiveUsersByCriteria(isNull(), any())).willReturn(users)

      val result = client.searchActiveUsers(fullName, userName)

      val inOrder = inOrder(webDriverNavigation, adminPage, caseworkerAdminPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditCaseworker()
      then(caseworkerAdminPage).should(inOrder).extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `given user extraction return multiple results`() {
    runBlocking {
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(caseworkerAdminPage.extractActiveUsers()).willReturn(users)

      val result = client.retrieveActiveUsers()

      val inOrder = inOrder(webDriverNavigation, adminPage, caseworkerAdminPage)
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/login")
      then(webDriverNavigation).should(inOrder).to("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditCaseworker()
      then(caseworkerAdminPage).should(inOrder).extractActiveUsers()
      assertEquals(users, result)
    }
  }

  private fun assertThatLogsOnAndVerifiesSuccess(asAdmin: Boolean = false) {
    val inOrder = inOrder(loginPage, searchPage)
    if (!asAdmin) {
      then(loginPage).should(inOrder).login(ppudUsername, ppudPassword)
    } else {
      then(loginPage).should(inOrder).login(ppudAdminUsername, ppudAdminPassword)
    }

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

  private fun setUpMocksToReturnSearchResultsWithInvalidOffender(
    searchResultLinks: List<String>,
    searchResultOffenders: List<SearchResultOffender>,
  ) {
    given(searchPage.searchResultsCount()).willReturn(searchResultLinks.size)
    given(searchPage.searchResultsLinks()).willReturn(searchResultLinks)
    val responsesWithInvalid = listOf(null, *searchResultOffenders.toTypedArray())
    given(offenderPage.extractSearchResultOffenderDetails()).willReturnConsecutively(responsesWithInvalid)
  }
}
