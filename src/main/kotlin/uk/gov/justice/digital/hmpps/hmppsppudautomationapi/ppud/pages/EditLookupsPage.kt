package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.Select
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName

/*
 * These refer to the IDs that appear on the tables in PPUD
 *
 * If you're adding a new reference data endpoint, to find the appropriate ID:
 *   1. Login to the ppud test instance
 *   2. Go to Admin then Lookup Edit
 *   3. Select the required LOV (list of values) from the select at top
 *   4. Use the web inspector to get the ID from the table
 */
@Component
internal class EditLookupsPage(driver: WebDriver) {

  @FindBy(id = "content_ddlLOVList")
  private lateinit var lookupTypeDropdown: WebElement

  @FindBy(id = "content_grdLOV")
  private lateinit var lookupsContentGridLov: WebElement

  @FindBy(id = "grdLOV")
  private lateinit var lookupsGridLov: WebElement

  @FindBy(id = "content_grdExtraBit")
  private lateinit var lookupsGridExtraBit: WebElement

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
      LookupName.Establishments to LookupConfig("Establishment", lookupsContentGridLov, 4),
      LookupName.Ethnicities to LookupConfig("Ethnicity", lookupsContentGridLov, 2),
      LookupName.IndexOffences to LookupConfig("Index Offence", lookupsGridExtraBit, 2),
      LookupName.MappaLevels to LookupConfig("Mappa Level", lookupsContentGridLov, 2),
      LookupName.PoliceForces to LookupConfig("Police Force", lookupsContentGridLov, 2),
      LookupName.ProbationServices to LookupConfig("Probation Service", lookupsGridExtraBit, 2),
      LookupName.ReleasedUnders to LookupConfig("Released Under", lookupsContentGridLov, 2),
      LookupName.Courts to LookupConfig("Court", lookupsGridLov, 2),
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
