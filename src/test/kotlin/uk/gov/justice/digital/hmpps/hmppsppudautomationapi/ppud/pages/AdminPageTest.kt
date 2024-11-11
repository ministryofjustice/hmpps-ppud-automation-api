package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

class AdminPageTest {

  @Mock
  private lateinit var driver: WebDriver

  private lateinit var adminPage: AdminPage

  private lateinit var editLookupsLink: WebElement

  @BeforeEach
  fun beforeEach() {
    driver = mock(WebDriver::class.java)
    adminPage = mock(AdminPage::class.java)
    editLookupsLink = mock(WebElement::class.java)
  }

  @Test
  fun `goToEditLookups should call editLookupsLink`() {
    runBlocking {
      doNothing().`when`(adminPage).goToEditLookups()
      adminPage.goToEditLookups()
    }
  }

  @Test
  fun `goToEditCaseworker should call goToEditCaseworker`() {
    runBlocking {
      doNothing().`when`(adminPage).goToEditLookups()
      adminPage.goToEditCaseworker()
    }
  }
}
