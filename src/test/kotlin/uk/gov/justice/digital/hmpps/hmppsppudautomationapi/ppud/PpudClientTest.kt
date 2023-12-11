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
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.willReturnConsecutively
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RecallSummary
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

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
  private lateinit var recallPage: RecallPage

  private val ppudUrl = "https://ppud.example.com"

  private lateinit var ppudUsername: String

  private lateinit var ppudPassword: String

  private lateinit var client: PpudClient

  @BeforeEach
  fun beforeEach() {
    ppudUsername = randomString("username")
    ppudPassword = randomString("password")
    client = PpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      driver,
      loginPage,
      adminPage,
      editLookupsPage,
      searchPage,
      offenderPage,
      recallPage,
    )
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
      val offender = generateOffender(croNumber = croNumber)
      setUpMocksToReturnSingleSearchResult(searchResultLink, offender)

      val result = client.searchForOffender(croNumber, null, null, null)

      then(driver).should().get(searchResultLink)
      then(offenderPage).should().extractOffenderDetails()
      assertEquals(offender, result.single())
    }
  }

  @Test
  fun `given NOMS ID that returns single result when search offender is called then return offender details`() {
    runBlocking {
      val nomsId = randomNomsId()
      val searchResultLink = "/link/to/offender/details/matching/nomsId"
      val offender = generateOffender(nomsId = nomsId)
      setUpMocksToReturnSingleSearchResult(searchResultLink, offender)

      val result = client.searchForOffender(null, nomsId, null, null)

      then(driver).should().get(searchResultLink)
      then(offenderPage).should().extractOffenderDetails()
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
        generateOffender(familyName = familyName, dateOfBirth = dateOfBirth),
        generateOffender(familyName = familyName, dateOfBirth = dateOfBirth),
      )
      setUpMocksToReturnMultipleSearchResults(searchResultLinks, offenders)

      val result = client.searchForOffender(null, null, familyName, dateOfBirth)

      val inOrder = inOrder(driver, offenderPage)
      then(driver).should(inOrder).get(searchResultLinks[0])
      then(offenderPage).should(inOrder).extractOffenderDetails()
      then(driver).should(inOrder).get(searchResultLinks[1])
      then(offenderPage).should(inOrder).extractOffenderDetails()
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
      given(recallPage.extractRecallSummaryDetails()).willReturn(RecallSummary(recallId))

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
      given(recallPage.extractRecallSummaryDetails()).willReturn(RecallSummary(recallId))

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
  fun `given ID when retrieveRecall is called then navigate to recall and extract details`() {
    runBlocking {
      val recallId = randomPpudId()
      val recall = generateRecall(id = recallId)
      val urlForId = "/something/recall?data=$recallId"
      given(loginPage.urlPath).willReturn("/login")
      given(recallPage.urlFor(recallId)).willReturn(urlForId)
      given(recallPage.extractRecallDetails()).willReturn(recall)
      val result = client.retrieveRecall(recallId)

      val inOrder = inOrder(driver, recallPage)
      then(driver).should(inOrder).get("$ppudUrl/login")
      then(driver).should(inOrder).get("$ppudUrl$urlForId")
      then(recallPage).should(inOrder).extractRecallDetails()
      assertEquals(recall, result)
    }
  }

  @Test
  fun `when retrieveLookupValues is called then navigate to edit lookups and extract details`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      val lookupName = randomString("lookupName")
      given(loginPage.urlPath).willReturn("/login")
      given(adminPage.urlPath).willReturn("/adminPage")
      given(editLookupsPage.extractLookupValues(lookupName)).willReturn(values)

      val result = client.retrieveLookupValues(lookupName)

      val inOrder = inOrder(driver, adminPage, editLookupsPage)
      then(driver).should(inOrder).get("$ppudUrl/login")
      then(driver).should(inOrder).get("$ppudUrl/adminPage")
      then(adminPage).should(inOrder).goToEditLookups()
      then(editLookupsPage).should(inOrder).extractLookupValues(lookupName)
      assertEquals(values, result)
    }
  }

  private fun setUpMocksToReturnSingleSearchResult(
    searchResultLink: String,
    offender: Offender,
  ) {
    given(searchPage.searchResultsCount()).willReturn(1)
    given(searchPage.searchResultsLinks()).willReturn(listOf(searchResultLink))
    given(offenderPage.extractOffenderDetails()).willReturn(offender)
  }

  private fun setUpMocksToReturnMultipleSearchResults(
    searchResultLinks: List<String>,
    offenders: List<Offender>,
  ) {
    given(searchPage.searchResultsCount()).willReturn(searchResultLinks.size)
    given(searchPage.searchResultsLinks()).willReturn(searchResultLinks)
    given(offenderPage.extractOffenderDetails()).willReturnConsecutively(offenders)
  }

  private fun generateOffender(
    croNumber: String? = null,
    nomsId: String? = null,
    familyName: String? = null,
    dateOfBirth: LocalDate? = null,
  ): Offender {
    return Offender(
      id = randomString("id"),
      croNumber = croNumber ?: randomCroNumber(),
      nomsId = nomsId ?: randomNomsId(),
      firstNames = randomString("firstNames"),
      familyName = familyName ?: randomString("familyName"),
      dateOfBirth = dateOfBirth ?: randomDate(),
    )
  }
}
