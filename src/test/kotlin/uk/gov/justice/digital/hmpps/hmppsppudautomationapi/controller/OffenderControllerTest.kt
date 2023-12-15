package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.time.LocalDate

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
      given(ppudClient.searchForOffender(croNumber, nomsId, familyName, dateOfBirth)).willReturn(emptyList())

      controller.search(criteria)

      then(ppudClient).should().searchForOffender(croNumber, nomsId, familyName, dateOfBirth)
    }
  }

  @Test
  fun `given recall data when createRecall is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val recallRequest = generateCreateRecallRequest()
      given(ppudClient.createRecall(offenderId, recallRequest)).willReturn(CreatedRecall(""))

      controller.createRecall(offenderId, recallRequest)

      then(ppudClient).should().createRecall(offenderId, recallRequest)
    }
  }

  @Test
  fun `given recall creation succeeds when createRecall is called then recall Id is returned`() {
    runBlocking {
      val offenderId = randomPpudId()
      val recallId = randomPpudId()
      val recallRequest = generateCreateRecallRequest()
      given(ppudClient.createRecall(offenderId, recallRequest)).willReturn(CreatedRecall(recallId))

      val result = controller.createRecall(offenderId, recallRequest)

      assertEquals(HttpStatus.CREATED, result.statusCode)
      assertEquals(recallId, result.body?.recall?.id)
    }
  }
}
