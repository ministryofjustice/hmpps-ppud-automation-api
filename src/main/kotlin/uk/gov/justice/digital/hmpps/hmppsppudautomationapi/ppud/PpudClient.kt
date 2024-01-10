package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WindowType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ApplicationControlPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SentencePageFactory
import java.time.LocalDate

@Component
@RequestScope
internal class PpudClient(
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.username}") private val ppudUsername: String,
  @Value("\${ppud.password}") private val ppudPassword: String,
  @Value("\${ppud.admin.username}") private val ppudAdminUsername: String,
  @Value("\${ppud.admin.password}") private val ppudAdminPassword: String,
  private val driver: WebDriver,
  private val applicationControlPage: ApplicationControlPage,
  private val loginPage: LoginPage,
  private val adminPage: AdminPage,
  private val editLookupsPage: EditLookupsPage,
  private val searchPage: SearchPage,
  private val offenderPage: OffenderPage,
  private val newOffenderPage: NewOffenderPage,
  private val sentencePageFactory: SentencePageFactory,
  private val recallPage: RecallPage,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun searchForOffender(
    croNumber: String?,
    nomsId: String?,
    familyName: String?,
    dateOfBirth: LocalDate?,
  ): List<SearchResultOffender> {
    log.info("Searching in PPUD Client")

    return performLoggedInOperation {
      val resultLinks = searchUntilFound(croNumber, nomsId, familyName, dateOfBirth)
      resultLinks.map { extractSearchResultOffenderDetails(it) }
    }
  }

  suspend fun createOffender(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    log.info("Creating new offender in PPUD Client")

    return performLoggedInOperation {
      createNewOffender(createOffenderRequest)
    }
  }

  suspend fun retrieveOffender(id: String): Offender {
    log.info("Retrieving offender in PPUD Client with ID '$id'")

    return performLoggedInOperation {
      offenderPage.viewOffenderWithId(id)
      offenderPage.extractOffenderDetails { extractSentences(it) }
    }
  }

  suspend fun createRecall(offenderId: String, recallRequest: CreateRecallRequest): CreatedRecall {
    log.info("Creating new recall in PPUD Client")

    return performLoggedInOperation {
      createNewRecall(offenderId, recallRequest)
    }
  }

  suspend fun retrieveRecall(id: String): Recall {
    log.info("Retrieving recall in PPUD Client with ID '$id'")

    return performLoggedInOperation {
      extractRecallDetails(id)
    }
  }

  suspend fun deleteRecalls(offenderId: String, sentenceDate: LocalDate, releaseDate: LocalDate) {
    log.info("Deleting recalls in PPUD Client for offender ID '$offenderId'")

    performLoggedInOperation {
      val links = extractRecallLinks(offenderId, sentenceDate, releaseDate)
      deleteRecalls(links)
    }
  }

  suspend fun retrieveLookupValues(lookupName: LookupName): List<String> {
    log.info("Retrieving lookup values for $lookupName")

    return performLoggedInOperation(asAdmin = true) {
      extractLookupValues(lookupName)
    }
  }

  private suspend fun <T> performLoggedInOperation(asAdmin: Boolean = false, operation: suspend () -> T): T {
    if (asAdmin) {
      loginAsAdmin()
    } else {
      login()
    }
    val result = operation()
    logout()
    return result
  }

  private suspend fun loginAsAdmin() {
    login(ppudAdminUsername, ppudAdminPassword)
  }

  private suspend fun login(username: String = ppudUsername, password: String = ppudPassword) {
    driver.navigate().to("${ppudUrl}${loginPage.urlPath}")
    loginPage.verifyOn()
    loginPage.login(username, password)
    searchPage.verifyOn()
  }

  private suspend fun logout() {
    applicationControlPage.logout()
    loginPage.verifyOn()
  }

  private suspend fun searchUntilFound(
    croNumber: String?,
    nomsId: String?,
    familyName: String?,
    dateOfBirth: LocalDate?,
  ): List<String> {
    if (!croNumber.isNullOrBlank()) {
      searchPage.searchByCroNumber(croNumber)
    }

    if (searchPage.searchResultsCount() == 0 && !nomsId.isNullOrBlank()) {
      searchPage.searchByNomsId(nomsId)
    }

    if (searchPage.searchResultsCount() == 0 && !familyName.isNullOrBlank() && dateOfBirth != null) {
      searchPage.searchByPersonalDetails(familyName, dateOfBirth)
    }

    return searchPage.searchResultsLinks()
  }

  private suspend fun createNewOffender(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    searchPage.navigateToNewOffender()
    newOffenderPage.verifyOn()
    newOffenderPage.createOffender(createOffenderRequest)
    newOffenderPage.throwIfInvalid()
    return offenderPage.extractCreatedOffenderDetails()
  }

  private suspend fun createNewRecall(
    offenderId: String,
    recallRequest: CreateRecallRequest,
  ): CreatedRecall {
    offenderPage.viewOffenderWithId(offenderId)
    offenderPage.navigateToNewRecallFor(recallRequest.sentenceDate, recallRequest.releaseDate)
    recallPage.createRecall(recallRequest)
    recallPage.throwIfInvalid()
    recallPage.addDetailsMinute(recallRequest)
    recallPage.addContrabandMinuteIfNeeded(recallRequest)
    return recallPage.extractCreatedRecallDetails()
  }

  private suspend fun deleteRecalls(links: List<String>) {
    var index = 1
    for (link in links) {
      log.info("Deleting recall $index $link")
      driver.switchTo().newWindow(WindowType.TAB)
      driver.navigate().to("${ppudUrl}$link")
      recallPage.deleteRecall()
      index++
    }
  }

  private suspend fun extractSearchResultOffenderDetails(url: String): SearchResultOffender {
    driver.navigate().to(url)
    return offenderPage.extractSearchResultOffenderDetails()
  }

  private fun extractSentences(urls: List<String>): List<Sentence> {
    return urls.map {
      driver.navigate().to("$ppudUrl$it")
      val page = sentencePageFactory.sentencePage()
      page.extractSentenceDetails()
    }
  }

  private suspend fun extractRecallDetails(id: String): Recall {
    driver.navigate().to("$ppudUrl${recallPage.urlFor(id)}")
    return recallPage.extractRecallDetails()
  }

  private fun extractRecallLinks(
    offenderId: String,
    sentenceDate: LocalDate,
    releaseDate: LocalDate,
  ): List<String> {
    offenderPage.viewOffenderWithId(offenderId)
    val links = offenderPage.extractRecallLinks(sentenceDate, releaseDate)
    log.info("There are ${links.size} recalls")
    return links
  }

  private suspend fun extractLookupValues(lookupName: LookupName): List<String> {
    return if (lookupName == LookupName.Genders) {
      extractGenderLookupValues()
    } else {
      extractAdminPageLookupValues(lookupName)
    }
  }

  private fun extractGenderLookupValues(): List<String> {
    return searchPage.genderValues()
  }

  private fun extractAdminPageLookupValues(lookupName: LookupName): List<String> {
    driver.navigate().to("$ppudUrl${adminPage.urlPath}")
    adminPage.verifyOn()
    adminPage.goToEditLookups()
    return editLookupsPage.extractLookupValues(lookupName)
  }
}
