package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId

@ExtendWith(MockitoExtension::class)
internal class RecallControllerTest {

  @Mock
  lateinit var ppudClient: OperationalPpudClient

  private lateinit var controller: RecallController

  @BeforeEach
  fun beforeEach() {
    controller = RecallController(ppudClient)
  }

  @Test
  fun `given recall ID when get is called then PPUD client is called with ID`() {
    runBlocking {
      val id = randomPpudId()
      given(ppudClient.retrieveRecall(id)).willReturn(generateRecall(id = id))

      controller.get(id)

      then(ppudClient).should().retrieveRecall(id)
    }
  }

  @Test
  fun `given recall id and document data when uploadMandatoryDocument is called then data is passed to PPUD client`() {
    runBlocking {
      val recallId = randomPpudId()
      val request = generateUploadMandatoryDocumentRequest()

      controller.uploadMandatoryDocument(recallId, request)

      then(ppudClient).should()
        .uploadMandatoryDocument(eq(recallId), eq(request), any())
    }
  }
}
