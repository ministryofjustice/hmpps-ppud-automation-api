package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage

@Component
internal class ReferenceDataPpudClient(
  @Value("\${ppud.url}") ppudUrl: String,
  @Value("\${ppud.username}") ppudUsername: String,
  @Value("\${ppud.password}") ppudPassword: String,
  @Value("\${ppud.admin.username}") ppudAdminUsername: String,
  @Value("\${ppud.admin.password}") ppudAdminPassword: String,
  driver: WebDriver,
  errorPage: ErrorPage,
  loginPage: LoginPage,
  searchPage: SearchPage,
  private val adminPage: AdminPage,
  private val editLookupsPage: EditLookupsPage,
) : PpudClientBase(
  ppudUrl,
  ppudUsername,
  ppudPassword,
  ppudAdminUsername,
  ppudAdminPassword,
  driver,
  errorPage,
  loginPage,
  searchPage,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun retrieveLookupValues(lookupName: LookupName): List<String> {
    log.info("Retrieving lookup values for $lookupName")

    return performLoggedInOperation(asAdmin = true) {
      extractLookupValues(lookupName)
    }
  }

  private suspend fun extractLookupValues(lookupName: LookupName): List<String> {
    return if (lookupName == LookupName.Genders) {
      extractGenderLookupValues()
    } else {
      extractAdminPageLookupValues(lookupName)
    }
  }

  private fun extractGenderLookupValues(): List<String> {
    return searchPage.genderValues()
  }

  private fun extractAdminPageLookupValues(lookupName: LookupName): List<String> {
    driver.navigate().to("$ppudUrl${adminPage.urlPath}")
    adminPage.verifyOn()
    adminPage.goToEditLookups()
    return editLookupsPage.extractLookupValues(lookupName)
  }
}
