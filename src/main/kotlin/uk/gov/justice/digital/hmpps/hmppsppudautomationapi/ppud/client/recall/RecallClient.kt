package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.recall

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import java.time.LocalDateTime

@Service
internal class RecallClient {

  @Autowired
  private lateinit var ppudClientConfig: PpudClientConfig

  @Autowired
  private lateinit var recallPage: RecallPage

  @Autowired
  private lateinit var offenderPage: OffenderPage

  @Autowired
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Autowired
  private lateinit var driver: WebDriver

  fun createRecall(
    offenderId: String,
    releaseId: String,
    recallRequest: CreateRecallRequest,
  ): CreatedRecall {
    offenderPage.viewOffenderWithId(offenderId)
    val foundMatch = navigateToMatchingRecall(releaseId, recallRequest.receivedDateTime, recallRequest.recommendedTo)

    // We only make changes if we don't find a matching Recall. If we do find one, it means the
    // recall has already been processed in PPUD. The likely reason we are encountering a matching
    // one is that something in the booking process failed after the Recall was added to PPUD and
    // a reattempt has been triggered (i.e. we're matching on the Recall created by CaR in a
    // previous attempt)
    if (foundMatch.not()) {
      navigationTreeViewComponent.navigateToNewRecallFor(releaseId)
      recallPage.createRecall(recallRequest)
      recallPage.throwIfInvalid()
      recallPage.addContrabandMinuteIfNeeded(recallRequest)

      // ID in URL after creating a new one is not the correct ID for the persisted recall.
      // Find the matching recall to extract the release ID from that
      navigateToMatchingRecall(releaseId, recallRequest.receivedDateTime, recallRequest.recommendedTo)
    }

    return recallPage.extractCreatedRecallDetails()
  }

  private fun navigateToMatchingRecall(
    releaseId: String,
    receivedDateTime: LocalDateTime,
    recommendedTo: PpudUser,
  ): Boolean {
    val releaseLinks = navigationTreeViewComponent.extractRecallLinks(releaseId)
    return releaseLinks.any {
      driver.navigate().to("${ppudClientConfig.url}$it")
      recallPage.isMatching(receivedDateTime, recommendedTo)
    }
  }

}