package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PpudClientTest {

  @Mock
  lateinit var loginPage: LoginPage

  @Test
  fun `given search criteria when search offender is called then log in to PPUD`() {
    runBlocking {
      val ppudUrl = "https://ppud.example.com"
      val client = PpudClient(ppudUrl)

      client.searchForOffender("cro", "noms", "familyName", LocalDate.parse("2000-01-01"))

      then(loginPage.login("", ""))
    }
  }
}
