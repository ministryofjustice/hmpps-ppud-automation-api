package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
class OffenderServiceTest {

  @InjectMocks
  private lateinit var offenderService: OffenderService

  @Mock
  private lateinit var authClient: PpudAuthClient

  @Test
  fun `creates an offender`() {
    runTest {
      // given
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOffenderRequest()
      val expectedCreatedSentence = CreatedSentence(sentenceId)
      val expectedCreatedOffender = CreatedOffender(
        offenderId,
        expectedCreatedSentence,
      )
      val methodCaptor = argumentCaptor<Supplier<CreatedOffender>>()

      given(authClient.performLoggedInOperation(eq(false), eq(false), any<Supplier<CreatedOffender>>())).willReturn(
        expectedCreatedOffender,
      )

      // when
      val actualOffenderCreated = offenderService.createOffender(request)

      // then
      then(authClient).should().performLoggedInOperation(eq(false), eq(false), methodCaptor.capture())
      assertEquals(expectedCreatedOffender, actualOffenderCreated)
    }
  }
}
