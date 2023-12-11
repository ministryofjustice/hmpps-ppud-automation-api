package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName

@Component
@RequestScope
internal class EditLookupsPage(driver: WebDriver) {

  @FindBy(id = "content_ddlLOVList")
  private lateinit var lookupTypeDropdown: WebElement

  @FindBy(id = "content_grdLOV")
  private lateinit var lookupsTable: WebElement

  private val columnMap: Map<LookupName, Int> = mapOf(
    LookupName.Establishment to 4,
    LookupName.Ethnicity to 2,
  )

  init {
    PageFactory.initElements(driver, this)
  }

  fun extractLookupValues(lookupName: LookupName): List<String> {
    selectLookupType(lookupName)
    val rows = lookupsTable.findElements(By.xpath(".//tr"))
    rows.removeFirst()
    val column = columnMap[lookupName]
    return rows
      .filter { it.findElement(By.xpath(".//td[last()]")).text == "Delete" }
      .map { it.findElement(By.xpath(".//td[$column]")).text }
  }

  private fun selectLookupType(lookupType: LookupName) {
    Select(lookupTypeDropdown).selectByVisibleText(lookupType.name)
  }
}
