package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease.PostReleaseClient
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
  private lateinit var postReleaseClient: PostReleaseClient

  fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ): CreatedOrUpdatedRelease {
    offenderPage.viewOffenderWithId(offenderId)
    val foundMatch = navigateToMatchingRelease(
      sentenceId,
      createOrUpdateReleaseRequest.dateOfRelease,
      createOrUpdateReleaseRequest.releasedFrom,
      createOrUpdateReleaseRequest.releasedUnder,
    )
    if (foundMatch) {
      releasePage.updateRelease()
    } else {
      navigationTreeViewComponent.navigateToNewOrEmptyReleaseFor(sentenceId)
      releasePage.createRelease(createOrUpdateReleaseRequest)
    }
    releasePage.throwIfInvalid()

    // ID in URL after creating a new one is not the correct ID for the persisted release.
    // Find the matching release and extract the release ID from that
    navigateToMatchingRelease(
      sentenceId,
      createOrUpdateReleaseRequest.dateOfRelease,
      createOrUpdateReleaseRequest.releasedFrom,
      createOrUpdateReleaseRequest.releasedUnder,
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
