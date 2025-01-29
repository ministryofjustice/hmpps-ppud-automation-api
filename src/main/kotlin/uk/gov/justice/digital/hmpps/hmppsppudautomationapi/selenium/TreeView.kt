package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class TreeView(rootElement: WebElement) {

  // XPath: all child divs whose ids don't start with 'M_' and have a following-sibling div
  private val expandableNodes =
    rootElement.findElements(By.xpath("./div[not(starts-with(@id, 'M_')) and boolean(./following-sibling::div)]"))

  fun expandNodeWithText(text: String): TreeViewNode = TreeViewNode(expandableNodes.first { it.text.trim() == text }).expandNode()

  fun nodeWithTextIsExpandable(text: String): Boolean = expandableNodes.indexOfFirst { it.text.trim() == text } > -1

  fun children(): List<TreeViewNode> = expandableNodes.map { TreeViewNode(it) }
}
