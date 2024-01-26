package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Release
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView

internal abstract class SentencePage(protected val driver: WebDriver) {

  @FindBy(id = "M_ctl00treetvOffender")
  protected lateinit var navigationTreeViewRoot: WebElement

  abstract fun extractSentenceDetails(
    includeEmptyReleases: Boolean,
    releaseExtractor: (List<String>) -> List<Release>,
  ): Sentence

  protected fun determineReleaseLinks(includeEmptyReleases: Boolean): List<String> {
    val sentenceNodes = TreeView(navigationTreeViewRoot)
      .expandNodeWithText("Sentences")
      .children()
      .filter { it.text.startsWith("New").not() }

    val thisSentenceNode = sentenceNodes.first {
      it.getAttribute("igurl").isNotEmpty() && driver.currentUrl.endsWith(it.getAttribute("igurl"))
    }

    val releaseNodes =
      thisSentenceNode.expandNode()
        .expandNodeWithText("Releases")
        .children()
        .filter {
          it.text.startsWith("New").not() &&
            (includeEmptyReleases || it.text.trim().startsWith("Not Specified").not())
        }

    return releaseNodes.map { it.getAttribute("igurl") }
  }
}
