package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class TreeView(rootElement: WebElement) {

  private val nodes =
    rootElement.findElements(By.xpath("./div"))
      .filter {
        it.getAttribute("id").orEmpty().startsWith("M_").not()
      }

  fun expandNodeWithText(text: String): TreeViewNode = TreeViewNode(nodes.first { it.text.trim() == text }).expandNode()

  fun children(): List<TreeViewNode> = nodes.map { TreeViewNode(it) }
}
