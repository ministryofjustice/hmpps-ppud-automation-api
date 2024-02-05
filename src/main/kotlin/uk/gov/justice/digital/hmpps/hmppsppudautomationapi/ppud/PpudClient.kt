package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.PostRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import java.time.LocalDate
import java.time.LocalDateTime

@Component
@RequestScope
internal class PpudClient(
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.username}") private val ppudUsername: String,
  @Value("\${ppud.password}") private val ppudPassword: String,
  @Value("\${ppud.admin.username}") private val ppudAdminUsername: String,
  @Value("\${ppud.admin.password}") private val ppudAdminPassword: String,
  private val driver: WebDriver,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
  private val adminPage: AdminPage,
  private val editLookupsPage: EditLookupsPage,
  private val loginPage: LoginPage,
  private val newOffenderPage: NewOffenderPage,
  private val offenderPage: OffenderPage,
  private val offencePage: OffencePage,
  private val postReleasePage: PostReleasePage,
  private val recallPage: RecallPage,
  private val releasePage: ReleasePage,
  private val searchPage: SearchPage,
  private val sentencePageFactory: SentencePageFactory,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val relativeLogoutUrl = "/logout.aspx"

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
      createOffenderInternal(createOffenderRequest)
    }
  }

  suspend fun updateOffender(offenderId: String, updateOffenderRequest: UpdateOffenderRequest) {
    log.info("Updating offender in PPUD Client")
    performLoggedInOperation {
      offenderPage.viewOffenderWithId(offenderId)
      offenderPage.updateOffender(updateOffenderRequest)
      offenderPage.throwIfInvalid()
    }
  }

  suspend fun retrieveOffender(id: String, includeEmptyReleases: Boolean = false): Offender {
    log.info("Retrieving offender in PPUD Client with ID '$id'")

    return performLoggedInOperation {
      offenderPage.viewOffenderWithId(id)
      offenderPage.extractOffenderDetails(extractSentences(includeEmptyReleases))
    }
  }

  suspend fun createSentence(offenderId: String, request: CreateOrUpdateSentenceRequest): CreatedSentence {
    log.info("Creating sentence in PPUD Client")
    return performLoggedInOperation {
      createSentenceInternal(offenderId, request)
    }
  }

  suspend fun updateSentence(offenderId: String, sentenceId: String, request: CreateOrUpdateSentenceRequest) {
    log.info("Updating sentence in PPUD Client")
    performLoggedInOperation {
      updateSentenceInternal(offenderId, sentenceId, request)
    }
  }

  suspend fun updateOffence(offenderId: String, sentenceId: String, request: UpdateOffenceRequest) {
    log.info("Updating offence in PPUD Client")
    performLoggedInOperation {
      updateOffenceInternal(offenderId, sentenceId, request)
    }
  }

  suspend fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ): CreatedOrUpdatedRelease {
    log.info("Creating/updating release in PPUD Client")

    return performLoggedInOperation {
      createOrUpdateReleaseInternal(offenderId, sentenceId, createOrUpdateReleaseRequest)

      // ID in URL after creating a new one is not the correct ID for the persisted release.
      // Find the matching release and extract the release ID from that
      navigateToMatchingRelease(
        sentenceId,
        createOrUpdateReleaseRequest.dateOfRelease,
        createOrUpdateReleaseRequest.releasedFrom,
        createOrUpdateReleaseRequest.releasedUnder,
      )
      val releaseId = releasePage.extractReleaseId()
      updatePostRelease(releaseId, createOrUpdateReleaseRequest)
      CreatedOrUpdatedRelease(releaseId)
    }
  }

  suspend fun createRecall(
    offenderId: String,
    releaseId: String,
    recallRequest: CreateRecallRequest,
  ): CreatedRecall {
    log.info("Creating new recall in PPUD Client")

    return performLoggedInOperation {
      createRecallInternal(offenderId, releaseId, recallRequest)
    }
  }

  suspend fun retrieveRecall(id: String): Recall {
    log.info("Retrieving recall in PPUD Client with ID '$id'")

    return performLoggedInOperation {
      extractRecallDetails(id)
    }
  }

  suspend fun deleteOffenders(familyName: String) {
    log.info("Deleting offenders in PPUD Client with family name '$familyName'")

    performLoggedInOperation {
      searchPage.searchByFamilyName(familyName)
      val links = searchPage.searchResultsLinks()
      deleteOffenders(links)
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
    val result = try {
      operation()
    } finally {
      logout()
    }
    return result
  }

  private suspend fun loginAsAdmin() {
    login(ppudAdminUsername, ppudAdminPassword)
  }

  private suspend fun login(username: String = ppudUsername, password: String = ppudPassword) {
    driver.navigate().to("${ppudUrl}${loginPage.urlPath}")
    loginPage.verifyOn()
    loginPage.login(username, password)
    loginPage.throwIfInvalid()
    searchPage.verifyOn()
  }

  private suspend fun logout() {
    try {
      driver.navigate().to("$ppudUrl$relativeLogoutUrl")
    } catch (ex: Exception) {
      log.error("Error attempting to log out of PPUD", ex)
    }
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

  private suspend fun createOffenderInternal(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    searchPage.navigateToNewOffender()
    newOffenderPage.verifyOn()
    newOffenderPage.createOffender(createOffenderRequest)
    newOffenderPage.throwIfInvalid()
    offenderPage.updateAdditionalAddresses(createOffenderRequest.additionalAddresses)
    offenderPage.throwIfInvalid()
    return offenderPage.extractCreatedOffenderDetails()
  }

  private fun createSentenceInternal(
    offenderId: String,
    request: CreateOrUpdateSentenceRequest,
  ): CreatedSentence {
    offenderPage.viewOffenderWithId(offenderId)
    val matched = navigateToMatchingSentence(request)

    if (!matched) {
      navigationTreeViewComponent.navigateToNewSentence()
      val newSentencePage = sentencePageFactory.sentencePage()
      newSentencePage.selectCustodyType(request.custodyType)
      val sentencePage = sentencePageFactory.sentencePage()
      sentencePage.createSentence(request)
      sentencePage.throwIfInvalid()
    }

    return sentencePageFactory.sentencePage().extractCreatedSentenceDetails()
  }

  private fun updateSentenceInternal(
    offenderId: String,
    sentenceId: String,
    request: CreateOrUpdateSentenceRequest,
  ) {
    offenderPage.viewOffenderWithId(offenderId)
    navigationTreeViewComponent.navigateToSentenceFor(sentenceId)
    val sentencePage = sentencePageFactory.sentencePage()
    sentencePage.updateSentence(request)
    sentencePage.throwIfInvalid()
  }

  private fun updateOffenceInternal(
    offenderId: String,
    sentenceId: String,
    request: UpdateOffenceRequest,
  ) {
    offenderPage.viewOffenderWithId(offenderId)
    navigationTreeViewComponent.navigateToOffenceFor(sentenceId)
    offencePage.updateOffence(request)
  }

  private fun createOrUpdateReleaseInternal(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ) {
    offenderPage.viewOffenderWithId(offenderId)
    val foundMatch = navigateToMatchingRelease(
      sentenceId,
      createOrUpdateReleaseRequest.dateOfRelease,
      createOrUpdateReleaseRequest.releasedFrom,
      createOrUpdateReleaseRequest.releasedUnder,
    )
    if (foundMatch) {
      releasePage.updateRelease(createOrUpdateReleaseRequest)
    } else {
      navigationTreeViewComponent.navigateToNewOrEmptyReleaseFor(sentenceId)
      releasePage.createRelease(createOrUpdateReleaseRequest)
    }
    releasePage.throwIfInvalid()
  }

  private fun updatePostRelease(
    releaseId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ) {
    postReleasePage.navigateToPostReleaseFor(releaseId)
    postReleasePage.updatePostRelease(createOrUpdateReleaseRequest.postRelease)
    postReleasePage.throwIfInvalid()
  }

  private suspend fun createRecallInternal(
    offenderId: String,
    releaseId: String,
    recallRequest: CreateRecallRequest,
  ): CreatedRecall {
    offenderPage.viewOffenderWithId(offenderId)
    val recallLinks = navigationTreeViewComponent.extractRecallLinks(releaseId)
    val foundMatch = recallLinks.any {
      driver.navigate().to("$ppudUrl$it")
      recallPage.isMatching(recallRequest.receivedDateTime, recallRequest.recommendedToOwner)
    }

    if (foundMatch.not()) {
      navigationTreeViewComponent.navigateToNewRecallFor(releaseId)
      recallPage.createRecall(recallRequest)
      recallPage.throwIfInvalid()
      recallPage.addDetailsMinute(recallRequest)
      recallPage.addContrabandMinuteIfNeeded(recallRequest)

      // ID in URL after creating a new one is not the correct ID for the persisted recall.
      // Find the matching recall to extract the release ID from that
      navigateToMatchingRecall(releaseId, recallRequest.receivedDateTime, recallRequest.recommendedToOwner)
    }

    return recallPage.extractCreatedRecallDetails()
  }

  private suspend fun deleteOffenders(absoluteLinks: List<String>) {
    var index = 1
    for (link in absoluteLinks) {
      log.info("Deleting offender $index $link")
      driver.navigate().to(link)
      offenderPage.deleteOffender()
      searchPage.verifyOn()
      index++
    }
  }

  private suspend fun extractSearchResultOffenderDetails(url: String): SearchResultOffender {
    driver.navigate().to(url)
    return offenderPage.extractSearchResultOffenderDetails()
  }

  private fun extractSentences(includeEmptyReleases: Boolean = false): (List<String>) -> List<Sentence> {
    return { urls ->
      urls.map {
        driver.navigate().to("$ppudUrl$it")
        val sentencePage = sentencePageFactory.sentencePage()
        sentencePage.extractSentenceDetails(includeEmptyReleases, ::extractOffenceDetails, ::extractReleases)
      }
    }
  }

  private fun extractOffenceDetails(link: String): Offence {
    driver.navigate().to("$ppudUrl$link")
    return offencePage.extractOffenceDetails()
  }

  private fun extractReleases(urls: List<String>): List<Release> {
    return urls.map {
      driver.navigate().to("$ppudUrl$it")
      releasePage.extractReleaseDetails(::extractPostReleaseDetails)
    }
  }

  private fun extractPostReleaseDetails(link: String): PostRelease {
    driver.navigate().to("$ppudUrl$link")
    return postReleasePage.extractPostReleaseDetails()
  }

  private suspend fun extractRecallDetails(id: String): Recall {
    driver.navigate().to("$ppudUrl${recallPage.urlFor(id)}")
    return recallPage.extractRecallDetails()
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

  private fun navigateToMatchingSentence(request: CreateOrUpdateSentenceRequest): Boolean {
    val sentenceLinks = navigationTreeViewComponent.extractSentenceLinks(request.dateOfSentence, request.custodyType)
    return sentenceLinks.any {
      driver.navigate().to("$ppudUrl$it")
      sentencePageFactory.sentencePage().isMatching(request)
    }
  }

  private fun navigateToMatchingRelease(
    sentenceId: String,
    dateOfRelease: LocalDate,
    releasedFrom: String,
    releasedUnder: String,
  ): Boolean {
    val releaseLinks = navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)
    return releaseLinks.any {
      driver.navigate().to("$ppudUrl$it")
      releasePage.isMatching(releasedFrom, releasedUnder)
    }
  }

  private fun navigateToMatchingRecall(
    releaseId: String,
    receivedDateTime: LocalDateTime,
    recommendedToOwner: String,
  ): Boolean {
    val releaseLinks = navigationTreeViewComponent.extractRecallLinks(releaseId)
    return releaseLinks.any {
      driver.navigate().to("$ppudUrl$it")
      recallPage.isMatching(receivedDateTime, recommendedToOwner)
    }
  }
}
