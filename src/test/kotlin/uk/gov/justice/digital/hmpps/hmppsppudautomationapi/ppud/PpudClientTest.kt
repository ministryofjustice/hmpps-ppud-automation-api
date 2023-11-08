package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.mockito.kotlin.whenever
import org.openqa.selenium.NotFoundException
import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var loginPage: LoginPage

  @Test
  fun `given search criteria when search offender is called then log in to PPUD`() {
    runBlocking {
      val ppudUrl = "https://ppud.example.com"
      val ppudUsername = randomString("username")
      val ppudPassword = randomString("password")
      val client = PpudClient(ppudUrl, ppudUsername, ppudPassword, driver, loginPage)

      client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))

      then(loginPage).should().login(ppudUsername, ppudPassword)
    }
  }

  @Test
  fun `given search criteria and PPUD login page is failing when search offender is called then exception is bubbled up`() {
    runBlocking {
      val ppudUrl = "https://ppud.example.com"
      val ppudUsername = randomString("username")
      val ppudPassword = randomString("password")
      val client = PpudClient(ppudUrl, ppudUsername, ppudPassword, driver, loginPage)
      whenever(loginPage.verifyOn()).thenThrow(NotFoundException())

      assertThrows<NotFoundException> {
        client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))
      }
    }
  }

  private fun randomString(prefix: String): String {
    return "$prefix-${UUID.randomUUID()}"
  }
}
