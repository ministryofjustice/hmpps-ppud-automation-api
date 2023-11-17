package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class OffenderControllerTest {

  @Mock
  lateinit var ppudClient: PpudClient

  private lateinit var controller: OffenderController

  @BeforeEach
  fun beforeEach() {
    controller = OffenderController(ppudClient)
  }

  @Test
  fun `given search criteria when search is called then criteria are passed to PPUD client`() {
    runBlocking {
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
      whenever(ppudClient.searchForOffender(croNumber, nomsId, familyName, dateOfBirth)).thenReturn(emptyList())

      controller.search(criteria)

      then(ppudClient).should().searchForOffender(croNumber, nomsId, familyName, dateOfBirth)
    }
  }

  @Test
  fun `given recall data when createRecall is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = UUID.randomUUID().toString()
      val recallRequest = CreateRecallRequest(
        sentenceDate = LocalDate.now(),
        releaseDate = LocalDate.now(),
      )
      whenever(ppudClient.createRecall(offenderId, recallRequest)).thenReturn(Recall(""))

      controller.createRecall(offenderId, recallRequest)

      then(ppudClient).should().createRecall(offenderId, recallRequest)
    }
  }

  @Test
  fun `given recall creation succeeds when createRecall is called then recall Id is returned`() {
    runBlocking {
      val offenderId = UUID.randomUUID().toString()
      val recallId = UUID.randomUUID().toString()
      val recallRequest = CreateRecallRequest(
        sentenceDate = LocalDate.now(),
        releaseDate = LocalDate.now(),
      )
      whenever(ppudClient.createRecall(offenderId, recallRequest)).thenReturn(Recall(recallId))

      val result = controller.createRecall(offenderId, recallRequest)

      assertEquals(HttpStatus.CREATED, result.statusCode)
      assertEquals(recallId, result.body?.recall?.id)
    }
  }
}
