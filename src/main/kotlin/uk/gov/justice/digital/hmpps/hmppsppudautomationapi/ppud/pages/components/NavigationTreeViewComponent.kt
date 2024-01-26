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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@RequestScope
class NavigationTreeViewComponent(
  driver: WebDriver,
  private val dateFormatter: DateTimeFormatter,
) {

  @FindBy(id = "M_ctl00treetvOffender")
  private lateinit var navigationTreeViewRoot: WebElement

  companion object {
    private const val SENTENCES_NODE_TEXT = "Sentences"
    private const val RELEASES_NODE_TEXT = "Releases"
    private const val POST_RELEASE_NODE_TEXT = "Post Release"
    private const val NEW_NODE_TEXT = "New..."
    private const val NOT_SPECIFIED_TEXT = "Not Specified"
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

  fun findRecallsFor(dateOfSentence: LocalDate, dateOfRelease: LocalDate): TreeViewNode {
    return TreeView(navigationTreeViewRoot)
      .expandNodeWithText("Sentences")
      .expandNodeWithTextContaining(dateOfSentence.format(dateFormatter))
      .expandNodeWithText("Releases")
      .expandNodeWithTextContaining(dateOfRelease.format(dateFormatter))
      .expandNodeWithTextContaining("Recalls")
  }

  fun navigateToNewOrEmptyReleaseFor(sentenceId: String) {
    val releasesNode = findSentenceNodeFor(sentenceId)
      .expandNodeWithText(RELEASES_NODE_TEXT)

    val resultNode = if (releasesNode.hasDescendentNodeWithTextContaining(NOT_SPECIFIED_TEXT)) {
      releasesNode.findNodeWithTextContaining(NOT_SPECIFIED_TEXT)
    } else {
      releasesNode.findNodeWithText(NEW_NODE_TEXT)
    }

    resultNode.click()
  }

  fun navigateToNewRecallFor(dateOfSentence: LocalDate, dateOfRelease: LocalDate) {
    findRecallsFor(dateOfSentence, dateOfRelease)
      .findNodeWithTextContaining(NEW_NODE_TEXT)
      .click()
  }

  fun extractReleaseLinks(sentenceId: String, dateOfRelease: LocalDate): List<String> {
    val sentenceNode = findSentenceNodeFor(sentenceId)

    return sentenceNode
      .expandNodeWithText(RELEASES_NODE_TEXT)
      .children()
      .filter { it.text.contains(dateOfRelease.format(dateFormatter)) }
      .map { it.url }
  }

  fun extractRecallLinks(dateOfSentence: LocalDate, dateOfRelease: LocalDate): List<String> {
    return findRecallsFor(dateOfSentence, dateOfRelease)
      .children()
      .excludeNewNode()
      .map { it.url }
  }

  private fun List<TreeViewNode>.excludeNewNode(): List<TreeViewNode> {
    return this.filter { it.text != NEW_NODE_TEXT }
  }

  private val TreeViewNode.url: String
    get() = this.getAttribute(URL_ATTRIBUTE)
}
