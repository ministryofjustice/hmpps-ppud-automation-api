package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.SentenceComparator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent.Companion.url
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper

internal abstract class SentencePage(
  protected val driver: WebDriver,
  protected val pageHelper: PageHelper,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
  private val sentenceComparator: SentenceComparator,
) {

  protected abstract val pageDescription: String

  abstract fun selectCustodyType(custodyType: String)

  fun isMatching(request: CreateOrUpdateSentenceRequest): Boolean {
    val existing = extractSentenceDetails(false, ::Offence) { emptyList() }
    return sentenceComparator.areMatching(existing, request)
  }

  abstract fun createSentence(request: CreateOrUpdateSentenceRequest)

  abstract fun updateSentence(request: CreateOrUpdateSentenceRequest)

  abstract fun extractCreatedSentenceDetails(): CreatedSentence

  abstract fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    offenceExtractor: (String) -> Offence,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence

  abstract fun throwIfInvalid()

  protected fun determineOffenceLink(): String {
    val sentenceId = pageHelper.extractId(pageDescription)
    return navigationTreeViewComponent
      .findOffenceNodeFor(sentenceId)
      .url
  }

  protected fun determineReleaseLinks(includeEmptyReleases: Boolean): List<String> {
    val sentenceId = pageHelper.extractId(pageDescription)
    return navigationTreeViewComponent.extractReleaseLinks(sentenceId, includeEmptyReleases)
  }
}
