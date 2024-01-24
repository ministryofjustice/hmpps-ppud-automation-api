package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components

import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ReleaseNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.SentenceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeViewNode

@Component
@RequestScope
class NavigationTreeViewComponent(driver: WebDriver) {

  @FindBy(id = "M_ctl00treetvOffender")
  private lateinit var navigationTreeViewRoot: WebElement

  companion object {
    private const val SENTENCES_NODE_TEXT = "Sentences"
    private const val RELEASES_NODE_TEXT = "Releases"
    private const val POST_RELEASE_NODE_TEXT = "Post Release"
    private const val NEW_NODE_TEXT = "New..."
    private const val URL_ATTRIBUTE = "igurl"
  }

  init {
    PageFactory.initElements(driver, this)
  }

  fun findSentenceNodeFor(sentenceId: String): TreeViewNode {
    val sentencesNode = TreeView(navigationTreeViewRoot).expandNodeWithText(SENTENCES_NODE_TEXT)

    val sentenceNode = try {
      sentencesNode.expandNodeWithLinkContaining(sentenceId)
    } catch (ex: NoSuchElementException) {
      throw SentenceNotFoundException("Sentence ID '$sentenceId' does not exist on this offender", ex)
    }
    return sentenceNode
  }

  fun findPostReleaseNodeFor(releaseId: String): TreeViewNode {
    val sentenceNodes = TreeView(navigationTreeViewRoot)
      .expandNodeWithText(SENTENCES_NODE_TEXT)
      .children()
      .excludeNewNode()

    val releaseNodes = sentenceNodes.flatMap {
      it.expandNode()
        .expandNodeWithText(RELEASES_NODE_TEXT)
        .children()
        .excludeNewNode()
    }

    val matchingRelease = releaseNodes.firstOrNull {
      it.url.contains("data=$releaseId")
    }

    if (matchingRelease == null) {
      throw ReleaseNotFoundException("Release ID '$releaseId' does not exist on offender")
    }

    return matchingRelease.expandNode().findNodeWithTextContaining(POST_RELEASE_NODE_TEXT)
  }

  private fun List<TreeViewNode>.excludeNewNode(): List<TreeViewNode> {
    return this.filter { it.text != NEW_NODE_TEXT }
  }

  private val TreeViewNode.url: String
    get() = this.getAttribute(URL_ATTRIBUTE)
}
