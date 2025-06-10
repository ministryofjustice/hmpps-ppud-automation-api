package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.AddMinuteRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadMandatoryDocumentRequest
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.SentencePageFactory
import java.time.LocalDate

@Component
@RequestScope
internal class OperationalPpudClient(
  @Value("\${ppud.url}") ppudUrl: String,
  @Value("\${ppud.username}") ppudUsername: String,
  @Value("\${ppud.password}") ppudPassword: String,
  @Value("\${ppud.admin.username}") ppudAdminUsername: String,
  @Value("\${ppud.admin.password}") ppudAdminPassword: String,
  driver: WebDriver,
  errorPage: ErrorPage,
  loginPage: LoginPage,
  searchPage: SearchPage,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
  private val newOffenderPage: NewOffenderPage,
  private val offenderPage: OffenderPage,
  private val offencePage: OffencePage,
  private val recallPage: RecallPage,
  private val offenceClient: OffenceClient,
  private val sentencePageFactory: SentencePageFactory,
  private val adminPage: AdminPage,
  private val caseworkerAdminPage: CaseworkerAdminPage,
) : PpudClientBase(
  ppudUrl,
  ppudUsername,
  ppudPassword,
  ppudAdminUsername,
  ppudAdminPassword,
  driver,
  errorPage,
  loginPage,
  searchPage,
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
      resultLinks.mapNotNull { extractSearchResultOffenderDetails(it) }
    }
  }

  suspend fun createOffender(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    log.info("Creating new offender in PPUD Client")

    return performLoggedInOperation(disableRetry = true) {
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

  suspend fun retrieveOffender(id: String): Offender {
    log.info("Retrieving offender in PPUD Client with ID '$id'")

    return performLoggedInOperation {
      offenderPage.viewOffenderWithId(id)
      offenderPage.extractOffenderDetails(extractSentences())
    }
  }

  suspend fun updateOffence(offenderId: String, sentenceId: String, request: UpdateOffenceRequest) {
    log.info("Updating offence in PPUD Client")
    performLoggedInOperation {
      updateOffenceInternal(offenderId, sentenceId, request)
    }
  }

  suspend fun uploadMandatoryDocument(
    recallId: String,
    uploadMandatoryDocumentRequest: UploadMandatoryDocumentRequest,
    filepath: String,
  ) {
    log.info("Uploading mandatory document in PPUD to recall with ID '$recallId'")
    performLoggedInOperation {
      driver.navigate().to("$ppudUrl${recallPage.urlFor(recallId)}")
      recallPage.uploadMandatoryDocument(uploadMandatoryDocumentRequest, filepath)
      recallPage.markMandatoryDocumentAsReceived(uploadMandatoryDocumentRequest.category)
      recallPage.throwIfInvalid()
    }
  }

  suspend fun uploadAdditionalDocument(
    recallId: String,
    uploadAdditionalDocumentRequest: UploadAdditionalDocumentRequest,
    filepath: String,
  ) {
    log.info("Uploading additional document in PPUD to recall with ID '$recallId'")
    performLoggedInOperation {
      driver.navigate().to("$ppudUrl${recallPage.urlFor(recallId)}")
      recallPage.uploadAdditionalDocument(uploadAdditionalDocumentRequest, filepath)
      recallPage.throwIfInvalid()
    }
  }

  suspend fun addMinute(recallId: String, request: AddMinuteRequest) {
    log.info("Adding minute in PPUD to recall with ID '$recallId'")
    performLoggedInOperation {
      driver.navigate().to("$ppudUrl${recallPage.urlFor(recallId)}")
      if (!recallPage.hasMatchingMinute(request.subject, request.text)) {
        recallPage.addMinute(request.subject, request.text)
      }
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

    performLoggedInOperation(asAdmin = true) {
      searchPage.searchByFamilyName(familyName)
      val links = searchPage.searchResultsLinks()
      deleteOffenders(links)
    }
  }

  suspend fun searchActiveUsers(fullName: String?, userName: String?): List<PpudUser> {
    log.info("Retrieving users matching fullName and/or userName")
    return performLoggedInOperation(asAdmin = true) {
      extractActiveUsersByCriteria(fullName, userName)
    }
  }

  suspend fun retrieveActiveUsers(): List<PpudUser> {
    log.info("Retrieving active users")
    return performLoggedInOperation(asAdmin = true) {
      extractActiveUsers()
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

    if (searchPage.searchResultsCount() == 0 && !familyName.isNullOrBlank()) {
      searchPage.searchByFamilyName(familyName)
    }

    return searchPage.searchResultsLinks()
  }

  private suspend fun createOffenderInternal(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    searchPage.navigateToNewOffender()
    newOffenderPage.verifyOn()
    newOffenderPage.createOffender(createOffenderRequest)
    newOffenderPage.throwIfInvalid()
    offenderPage.verifyOn()
    offenderPage.updateAdditionalAddresses(createOffenderRequest.additionalAddresses)
    offenderPage.throwIfInvalid()
    return offenderPage.extractCreatedOffenderDetails(::extractCreatedSentence)
  }

  private fun updateOffenceInternal(
    offenderId: String,
    sentenceId: String,
    request: UpdateOffenceRequest,
  ) {
    offenderPage.viewOffenderWithId(offenderId)
    navigationTreeViewComponent.navigateToOffenceFor(sentenceId)
    offencePage.verifyOn()
    offencePage.updateOffence(request)
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

  private suspend fun extractSearchResultOffenderDetails(url: String): SearchResultOffender? {
    driver.navigate().to(url)
    return offenderPage.extractSearchResultOffenderDetails()
  }

  private fun extractCreatedSentence(sentenceLink: String): CreatedSentence {
    driver.navigate().to("$ppudUrl$sentenceLink")
    val sentencePage = sentencePageFactory.sentencePage()
    return sentencePage.extractCreatedSentenceDetails()
  }

  private fun extractSentences(): (List<String>) -> List<Sentence> = { urls ->
    urls.map {
      driver.navigate().to("$ppudUrl$it")
      val sentencePage = sentencePageFactory.sentencePage()
      sentencePage.extractSentenceDetails(offenceClient::getOffence)
    }
  }

  private suspend fun extractRecallDetails(id: String): Recall {
    driver.navigate().to("$ppudUrl${recallPage.urlFor(id)}")
    return recallPage.extractRecallDetails()
  }

  private suspend fun extractActiveUsersByCriteria(fullName: String?, userName: String?): List<PpudUser> {
    driver.navigate().to("$ppudUrl${adminPage.urlPath}")
    adminPage.verifyOn()
    adminPage.goToEditCaseworker()
    return caseworkerAdminPage.extractActiveUsersByCriteria(fullName, userName)
  }

  private suspend fun extractActiveUsers(): List<PpudUser> {
    driver.navigate().to("$ppudUrl${adminPage.urlPath}")
    adminPage.verifyOn()
    adminPage.goToEditCaseworker()
    return caseworkerAdminPage.extractActiveUsers()
  }
}
