package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease.PostReleaseClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import java.time.LocalDate

@Service
internal class ReleaseClient {

  @Autowired
  private lateinit var ppudClientConfig: PpudClientConfig

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Autowired
  private lateinit var releasePage: ReleasePage

  @Autowired
  private lateinit var offenderPage: OffenderPage

  @Autowired
  private lateinit var sentenceClient: SentenceClient

  @Autowired
  private lateinit var postReleaseClient: PostReleaseClient

  fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ): CreatedOrUpdatedRelease {
    val sentence = sentenceClient.getSentence(offenderId, sentenceId)
    val custodyType = try {
      SupportedCustodyType.forFullName(sentence.custodyType)
    } catch (ex: NoSuchElementException) {
      throw UnsupportedCustodyTypeException("Sentence $sentenceId has an unsupported custody type: ${sentence.custodyType}")
    }
    val releasedUnder = custodyType.releasedUnder?.fullName ?: createOrUpdateReleaseRequest.releasedUnder
    offenderPage.viewOffenderWithId(offenderId)
    val foundMatch = navigateToMatchingRelease(
      sentenceId,
      createOrUpdateReleaseRequest.dateOfRelease,
      createOrUpdateReleaseRequest.releasedFrom,
      releasedUnder,
    )
    // It is possible the Release already exists in the system, either because of a failed
    // booking attempt from CaR (in which case finding a match is part of the reattempt) or
    // because a user has put it into the system in the past (e.g. a recall was initiated
    // but then cancelled, but the corresponding Release was added to the system before the
    // cancellation). In both cases it's OK to update/overwrite the existing Release
    if (foundMatch) {
      releasePage.updateRelease()
    } else {
      navigationTreeViewComponent.navigateToNewOrEmptyReleaseFor(sentenceId)
      // TODO pass in releasedUnder whenever support for creating new Indeterminate sentences is added
      releasePage.createRelease(createOrUpdateReleaseRequest)
    }
    releasePage.throwIfInvalid()

    // ID in URL after creating a new one is not the correct ID for the persisted release.
    // Find the matching release and extract the release ID from that
    navigateToMatchingRelease(
      sentenceId,
      createOrUpdateReleaseRequest.dateOfRelease,
      createOrUpdateReleaseRequest.releasedFrom,
      releasedUnder,
    )
    val releaseId = releasePage.extractReleaseId()
    postReleaseClient.updatePostRelease(releaseId, createOrUpdateReleaseRequest.postRelease)
    return CreatedOrUpdatedRelease(releaseId)
  }

  private fun navigateToMatchingRelease(
    sentenceId: String,
    dateOfRelease: LocalDate,
    releasedFrom: String,
    releasedUnder: String,
  ): Boolean {
    val releaseLinks = navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)
    return releaseLinks.any {
      driver.navigate().to("${ppudClientConfig.url}$it")
      releasePage.isMatching(releasedFrom, releasedUnder)
    }
  }
}
