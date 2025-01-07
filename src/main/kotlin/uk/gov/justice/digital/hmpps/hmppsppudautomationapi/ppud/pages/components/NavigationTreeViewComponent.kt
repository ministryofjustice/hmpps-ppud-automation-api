package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components

import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ReleaseNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.SentenceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeView
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeViewNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
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
    private const val RECALLS_NODE_TEXT = "Recalls"
    private const val NEW_NODE_TEXT = "New..."
    private const val OFFENCE_NODE_TEXT = "Offence"
    private const val NOT_SPECIFIED_TEXT = "Not Specified"
    private const val URL_ATTRIBUTE = "igurl"

    val TreeViewNode.url: String
      get() = this.getAttribute(URL_ATTRIBUTE).orEmpty()

    fun List<TreeViewNode>.excludeNewNode(): List<TreeViewNode> = this.filter { it.text != NEW_NODE_TEXT }
  }

  init {
    PageFactory.initElements(driver, this)
  }

  val sentenceNodes: List<TreeViewNode>
    get() {
      return TreeView(navigationTreeViewRoot)
        .expandNodeWithText(SENTENCES_NODE_TEXT)
        .children()
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

  fun findOffenceNodeFor(sentenceId: String): TreeViewNode = findSentenceNodeFor(sentenceId)
    .expandNode()
    .findNodeWithText(OFFENCE_NODE_TEXT)

  fun findReleaseNodesFor(sentenceId: String): List<TreeViewNode> = findSentenceNodeFor(sentenceId)
    .expandNode()
    .expandNodeWithText(RELEASES_NODE_TEXT)
    .children()

  fun findReleaseNodeFor(releaseId: String): TreeViewNode {
    val releaseNodes = sentenceNodes
      .excludeNewNode()
      .flatMap {
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

    return matchingRelease
  }

  fun findPostReleaseNodeFor(releaseId: String): TreeViewNode = findReleaseNodeFor(releaseId)
    .expandNode()
    .findNodeWithTextContaining(POST_RELEASE_NODE_TEXT)

  fun findRecallNodesFor(releaseId: String): List<TreeViewNode> = findReleaseNodeFor(releaseId)
    .expandNode()
    .expandNodeWithTextContaining(RECALLS_NODE_TEXT)
    .children()

  fun findRecallsNodeFor(releaseId: String): TreeViewNode = findReleaseNodeFor(releaseId)
    .expandNodeWithTextContaining(RECALLS_NODE_TEXT)

  fun navigateToNewSentence() {
    TreeView(navigationTreeViewRoot)
      .expandNodeWithText(SENTENCES_NODE_TEXT)
      .findNodeWithText(NEW_NODE_TEXT)
      .click()
  }

  fun navigateToSentenceFor(sentenceId: String) {
    findSentenceNodeFor(sentenceId)
      .click()
  }

  fun navigateToOffenceFor(sentenceId: String) {
    findOffenceNodeFor(sentenceId)
      .click()
  }

  fun navigateToNewOrEmptyReleaseFor(sentenceId: String) {
    val releasesNode = findSentenceNodeFor(sentenceId)
      .expandNodeWithText(RELEASES_NODE_TEXT)

    val resultNode =
      releasesNode.tryFindNodeWithTextContaining(NOT_SPECIFIED_TEXT) ?: releasesNode.findNodeWithText(NEW_NODE_TEXT)

    resultNode.click()
  }

  fun navigateToNewRecallFor(releaseId: String) = findRecallsNodeFor(releaseId)
    .findNodeWithText(NEW_NODE_TEXT)
    .click()

  fun extractSentenceLinks(dateOfSentence: LocalDate, custodyType: String): List<String> {
    val formattedDate = dateOfSentence.format(dateFormatter)
    return sentenceNodes
      .filter { it.text.trim() == "$formattedDate - $custodyType" }
      .map { it.url }
  }

  fun extractReleaseLinks(sentenceId: String, includeEmptyReleases: Boolean): List<String> = findReleaseNodesFor(sentenceId)
    .filter {
      it.text.startsWith(NEW_NODE_TEXT).not() &&
        (includeEmptyReleases || it.text.trim().startsWith(NOT_SPECIFIED_TEXT).not())
    }
    .map { it.url }

  fun extractReleaseLinks(sentenceId: String, dateOfRelease: LocalDate): List<String> = findReleaseNodesFor(sentenceId)
    .filter { it.text.contains(dateOfRelease.format(dateFormatter)) }
    .map { it.url }

  fun extractRecallLinks(releaseId: String): List<String> = findRecallNodesFor(releaseId)
    .excludeNewNode()
    .map { it.url }
}
