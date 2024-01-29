package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent

internal abstract class SentencePage(
  protected val driver: WebDriver,
  private val navigationTreeViewComponent: NavigationTreeViewComponent,
) {

  abstract fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence

  protected fun determineReleaseLinks(includeEmptyReleases: Boolean): List<String> {
    val sentenceNodes = navigationTreeViewComponent
      .sentenceNodes
      .filter { it.text.startsWith("New").not() }

    val thisSentenceNode = sentenceNodes.first {
      it.getAttribute("igurl").isNotEmpty() && driver.currentUrl.endsWith(it.getAttribute("igurl"))
    }

    val releaseNodes =
      thisSentenceNode
        .expandNode()
        .expandNodeWithText("Releases")
        .children()
        .filter {
          it.text.startsWith("New").not() &&
            (includeEmptyReleases || it.text.trim().startsWith("Not Specified").not())
        }

    return releaseNodes.map { it.getAttribute("igurl") }
  }
}
