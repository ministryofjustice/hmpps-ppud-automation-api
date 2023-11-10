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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var loginPage: LoginPage

  @Mock
  private lateinit var searchPage: SearchPage

  @Mock
  private lateinit var offenderPage: OffenderPage

  private val ppudUrl = "https://ppud.example.com"

  private lateinit var ppudUsername: String

  private lateinit var ppudPassword: String

  private lateinit var client: PpudClient

  @BeforeEach
  fun beforeEach() {
    ppudUsername = randomString("username")
    ppudPassword = randomString("password")
    client = PpudClient(ppudUrl, ppudUsername, ppudPassword, driver, loginPage, searchPage, offenderPage)
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
      val dateOfBirth = randomDateOfBirth()

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
      val dateOfBirth = randomDateOfBirth()
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
      val offender = createOffender(croNumber = croNumber)
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
      val offender = createOffender(nomsId = nomsId)
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
      val dateOfBirth = randomDateOfBirth()
      val searchResultLinks = listOf(
        "/link/to/offender/details/1",
        "/link/to/offender/details/2",
      )
      val offenders = listOf(
        createOffender(familyName = familyName, dateOfBirth = dateOfBirth),
        createOffender(familyName = familyName, dateOfBirth = dateOfBirth),
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

  private fun createOffender(
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
      dateOfBirth = dateOfBirth ?: randomDateOfBirth(),
    )
  }

  private fun randomString(prefix: String): String {
    return "$prefix-${UUID.randomUUID()}"
  }

  private fun randomCroNumber(): String {
    val serial = Random.nextInt(100000, 999999)
    val year = Random.nextInt(10, 23)
    return "$serial/${year}A"
  }

  private fun randomNomsId(): String {
    val serial = Random.nextInt(1000, 9999)
    return "A${serial}BC"
  }

  private fun randomDateOfBirth(): LocalDate {
    return LocalDate.parse("2005-01-01").minusDays(Random.nextLong(20000))
  }
}
