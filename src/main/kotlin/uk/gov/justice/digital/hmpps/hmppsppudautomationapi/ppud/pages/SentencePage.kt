package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.extractId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent.Companion.url

internal abstract class SentencePage(
  protected val driver: WebDriver,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
) {

  protected abstract val pageDescription: String

  abstract fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    offenceExtractor: (String) -> Offence,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence

  protected fun determineOffenceLink(): String {
    val sentenceId = extractId(driver, pageDescription)
    return navigationTreeViewComponent
      .findOffenceNodeFor(sentenceId)
      .url
  }

  protected fun determineReleaseLinks(includeEmptyReleases: Boolean): List<String> {
    val sentenceId = extractId(driver, pageDescription)
    return navigationTreeViewComponent.extractReleaseLinks(sentenceId, includeEmptyReleases)
  }
}
