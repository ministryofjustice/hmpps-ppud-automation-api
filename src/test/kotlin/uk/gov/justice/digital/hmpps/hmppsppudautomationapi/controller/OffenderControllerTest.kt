package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OffenderControllerTest {

  @Mock
  lateinit var ppudClient: PpudClient

  @Test
  fun `given search criteria when search is called then criteria are passed to PPUD client`() {
    runBlocking {
      val controller = OffenderController(ppudClient)
      val croNumber = "A123"
      val nomsId = "B456"
      val familyName = "Smith"
      val dateOfBirth = LocalDate.parse("2000-01-01")
      val criteria = OffenderSearchRequest(
        croNumber,
        nomsId,
        familyName,
        dateOfBirth,
      )
      whenever(ppudClient.searchForOffender(croNumber)).thenReturn(emptyList())

      controller.search(criteria)

      then(ppudClient).should().searchForOffender(croNumber)
    }
  }
}
