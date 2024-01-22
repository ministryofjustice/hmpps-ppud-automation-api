package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium

import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.Point
import org.openqa.selenium.Rectangle
import org.openqa.selenium.WebElement

open class TreeViewNode(private val element: WebElement) : WebElement {

  private val expansionElement by lazy { findElement(By.xpath("./following-sibling::div")) }

  private val expanderImage by lazy { findElement(By.xpath("./img[@imgtype='exp']")) }

  fun children(): List<TreeViewNode> {
    return expansionElement.findElements(By.xpath("./div"))
      .filter { it.isDisplayed }
      .map { TreeViewNode(it) }
  }

  fun expandNode(): TreeViewNode {
    if (expansionElement.isDisplayed.not()) {
      expanderImage.click()
    }
    return this
  }

  fun expandNodeWithText(text: String): TreeViewNode {
    return findNodeWithText(text).expandNode()
  }

  fun expandNodeWithTextContaining(text: String): TreeViewNode {
    return findNodeWithTextContaining(text).expandNode()
  }

  fun expandNodeWithLinkContaining(value: String): TreeViewNode {
    return findNodeWithLinkContaining(value).expandNode()
  }

  fun findNodeWithTextContaining(text: String): TreeViewNode {
    return TreeViewNode(expansionElement.findElement(By.xpath(".//*[contains(text(), '$text')]/parent::div")))
  }

  private fun findNodeWithLinkContaining(value: String): TreeViewNode {
    return TreeViewNode(expansionElement.findElement(By.xpath(".//div[contains(@igurl, '$value')]")))
  }

  private fun findNodeWithText(text: String): TreeViewNode {
    return TreeViewNode(expansionElement.findElement(By.xpath(".//*[text()='$text']/parent::div")))
  }

  override fun findElements(by: By?): MutableList<WebElement> {
    return element.findElements(by)
  }

  override fun findElement(by: By?): WebElement {
    return element.findElement(by)
  }

  override fun <X : Any?> getScreenshotAs(target: OutputType<X>?): X {
    return element.getScreenshotAs(target)
  }

  override fun click() {
    element.click()
  }

  override fun submit() {
    element.submit()
  }

  override fun sendKeys(vararg keysToSend: CharSequence?) {
    element.sendKeys()
  }

  override fun clear() {
    element.clear()
  }

  override fun getTagName(): String {
    return element.tagName
  }

  override fun getAttribute(name: String?): String {
    return element.getAttribute(name)
  }

  override fun isSelected(): Boolean {
    return element.isSelected
  }

  override fun isEnabled(): Boolean {
    return element.isEnabled
  }

  override fun getText(): String {
    return element.text
  }

  override fun isDisplayed(): Boolean {
    return element.isDisplayed
  }

  override fun getLocation(): Point {
    return element.location
  }

  override fun getSize(): Dimension {
    return element.size
  }

  override fun getRect(): Rectangle {
    return element.rect
  }

  override fun getCssValue(propertyName: String?): String {
    return element.getCssValue(propertyName)
  }
}
