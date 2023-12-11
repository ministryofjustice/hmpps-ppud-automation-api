package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RecallSummary
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import java.time.LocalDate

@Component
@RequestScope
internal class PpudClient(
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.username}") private val ppudUsername: String,
  @Value("\${ppud.password}") private val ppudPassword: String,
  private val driver: WebDriver,
  private val loginPage: LoginPage,
  private val adminPage: AdminPage,
  private val editLookupsPage: EditLookupsPage,
  private val searchPage: SearchPage,
  private val offenderPage: OffenderPage,
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
  ): List<Offender> {
    log.info("Searching in PPUD Client")

    login()

    val resultLinks = searchUntilFound(croNumber, nomsId, familyName, dateOfBirth)

    return resultLinks.map {
      extractOffenderDetails(it)
    }
  }

  suspend fun createRecall(offenderId: String, recallRequest: CreateRecallRequest): RecallSummary {
    log.info("Creating new recall in PPUD Client")

    login()

    return createNewRecall(offenderId, recallRequest)
  }

  suspend fun retrieveRecall(id: String): Recall {
    log.info("Retrieving recall in PPUD Client with ID '$id'")

    login()

    return extractRecallDetails(id)
  }

  suspend fun retrieveLookupValues(lookupName: LookupName): List<String> {
    log.info("Retrieving lookup values for $lookupName")

    login()

    return extractLookupValues(lookupName)
  }

  private suspend fun login() {
    driver.get("${ppudUrl}${loginPage.urlPath}")
    loginPage.verifyOn()
    loginPage.login(ppudUsername, ppudPassword)
    searchPage.verifyOn()
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

  private suspend fun createNewRecall(
    offenderId: String,
    recallRequest: CreateRecallRequest,
  ): RecallSummary {
    offenderPage.viewOffenderWithId(offenderId)
    offenderPage.navigateToNewRecallFor(recallRequest.sentenceDate, recallRequest.releaseDate)
    recallPage.createRecall(recallRequest)
    recallPage.throwIfInvalid()
    recallPage.addDetailsMinute(recallRequest)
    recallPage.addContrabandMinuteIfNeeded(recallRequest)
    return recallPage.extractRecallSummaryDetails()
  }

  private suspend fun extractOffenderDetails(url: String): Offender {
    driver.get(url)
    return offenderPage.extractOffenderDetails()
  }

  private suspend fun extractRecallDetails(id: String): Recall {
    driver.get("$ppudUrl${recallPage.urlFor(id)}")
    return recallPage.extractRecallDetails()
  }

  private suspend fun extractLookupValues(lookupName: LookupName): List<String> {
    return if (lookupName == LookupName.Gender) {
      extractGenderLookupValues()
    } else {
      extractAdminPageLookupValues(lookupName)
    }
  }

  private fun extractGenderLookupValues(): List<String> {
    return searchPage.genderValues()
  }

  private fun extractAdminPageLookupValues(lookupName: LookupName): List<String> {
    driver.get("$ppudUrl${adminPage.urlPath}")
    adminPage.verifyOn()
    adminPage.goToEditLookups()
    return editLookupsPage.extractLookupValues(lookupName)
  }
}
