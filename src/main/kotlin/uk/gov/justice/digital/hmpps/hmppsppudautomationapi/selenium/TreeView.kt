package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class TreeView(rootElement: WebElement) {

  // XPath: all child divs that have an img tag div with attribute 'imgtype' of value 'exp'
  private val expandableNodes =
    rootElement.findElements(By.xpath("//div[./img[@imgtype='exp']]"))

  fun expandNodeWithText(text: String): TreeViewNode = TreeViewNode(expandableNodes.first { it.text.trim() == text }).expandNode()

  fun nodeWithTextIsExpandable(text: String): Boolean = expandableNodes.indexOfFirst { it.text.trim() == text } > -1
}
