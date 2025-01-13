package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.Point
import org.openqa.selenium.Rectangle
import org.openqa.selenium.WebElement

private val WebElement.isExpansionElement: Boolean
  get() {
    return this.getAttribute("id").orEmpty().startsWith("M_")
  }

open class TreeViewNode(private val element: WebElement) : WebElement {

  private val expansionElement by lazy { findElement(By.xpath("./following-sibling::div")) }

  private val expanderImage by lazy { findElement(By.xpath("./img[@imgtype='exp']")) }

  private val nodeTextElement by lazy { findElement(By.xpath("./span[@igtxt='1']")) }

  fun children(): List<TreeViewNode> = expansionElement.findElements(By.xpath("./div"))
    .filter { !it.isExpansionElement }
    .map { TreeViewNode(it) }

  fun expandNode(): TreeViewNode {
    if (expansionElement.isDisplayed.not()) {
      expanderImage.click()
    }
    return this
  }

  fun expandNodeWithText(text: String): TreeViewNode = findNodeWithText(text).expandNode()

  fun expandNodeWithTextContaining(text: String): TreeViewNode = findNodeWithTextContaining(text).expandNode()

  fun expandNodeWithLinkContaining(value: String): TreeViewNode = findNodeWithLinkContaining(value).expandNode()

  fun findNodeWithTextContaining(text: String): TreeViewNode = TreeViewNode(expansionElement.findElement(By.xpath(xpathForNodeWithTextContaining(text))))

  fun findNodeWithText(text: String): TreeViewNode = TreeViewNode(expansionElement.findElement(By.xpath(".//*[text()='$text']/parent::div")))

  fun tryFindNodeWithTextContaining(text: String): TreeViewNode? {
    val matches = expansionElement.findElements(By.xpath(xpathForNodeWithTextContaining(text)))
    return if (matches.any()) TreeViewNode(matches.first()) else null
  }

  private fun findNodeWithLinkContaining(value: String): TreeViewNode = TreeViewNode(expansionElement.findElement(By.xpath(".//div[contains(@igurl, '$value')]")))

  private fun xpathForNodeWithTextContaining(text: String) = ".//*[contains(text(), '$text')]/parent::div"

  override fun findElements(by: By): MutableList<WebElement> = element.findElements(by)

  override fun findElement(by: By): WebElement = element.findElement(by)

  override fun <X : Any> getScreenshotAs(target: OutputType<X>): X = element.getScreenshotAs(target)

  override fun click() {
    nodeTextElement.click()
  }

  override fun submit() {
    element.submit()
  }

  override fun sendKeys(vararg keysToSend: CharSequence) {
    element.sendKeys()
  }

  override fun clear() {
    element.clear()
  }

  override fun getTagName(): String = element.tagName

  override fun getAttribute(name: String): String = element.getAttribute(name).orEmpty()

  override fun isSelected(): Boolean = element.isSelected

  override fun isEnabled(): Boolean = element.isEnabled

  override fun getText(): String = element.text

  override fun isDisplayed(): Boolean = element.isDisplayed

  override fun getLocation(): Point = element.location

  override fun getSize(): Dimension = element.size

  override fun getRect(): Rectangle = element.rect

  override fun getCssValue(propertyName: String): String = element.getCssValue(propertyName).orEmpty()
}
