package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName

@Component
internal class EditLookupsPage(driver: WebDriver) {

  @FindBy(id = "content_ddlLOVList")
  private lateinit var lookupTypeDropdown: WebElement

  @FindBy(id = "content_grdLOV")
  private lateinit var lookupsGridLov: WebElement

  @FindBy(id = "content_grdExtraBit")
  private lateinit var lookupsGridExtraBit: WebElement

  @FindBy(id = "content_grdAddressLov")
  private lateinit var lookupsGridAddressLov: WebElement

  private lateinit var configMap: Map<LookupName, LookupConfig>

  private data class LookupConfig(
    val dropdownText: String,
    val grid: WebElement,
    val columnNumber: Int,
  )

  init {
    PageFactory.initElements(driver, this)
    configMap = mapOf(
      LookupName.CustodyTypes to LookupConfig("Custody Type", lookupsGridExtraBit, 2),
      LookupName.Establishments to LookupConfig("Establishment", lookupsGridLov, 4),
      LookupName.Ethnicities to LookupConfig("Ethnicity", lookupsGridLov, 2),
      LookupName.IndexOffences to LookupConfig("Index Offence", lookupsGridExtraBit, 2),
      LookupName.MappaLevels to LookupConfig("Mappa Level", lookupsGridLov, 2),
      LookupName.PoliceForces to LookupConfig("Police Force", lookupsGridAddressLov, 2),
      LookupName.ProbationServices to LookupConfig("Probation Service", lookupsGridExtraBit, 2),
      LookupName.ReleasedUnders to LookupConfig("Released Under", lookupsGridLov, 2),
    )
  }

  fun extractLookupValues(lookupName: LookupName): List<String> {
    val config = configMap.getValue(lookupName)
    Select(lookupTypeDropdown).selectByVisibleText(config.dropdownText)
    val rows = config.grid.findElements(By.xpath(".//tr"))
    rows.removeFirst()
    return rows
      .filter { it.findElement(By.xpath(".//td[last()]")).text == "Delete" }
      .map { it.findElement(By.xpath(".//td[${config.columnNumber}]")).text }
  }
}
