package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offender

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender.validation.OffenderValidator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId

@ExtendWith(MockitoExtension::class)
class OffenderClientTest {

  @InjectMocks
  private lateinit var offenderClient: OffenderClient

  @Mock
  private lateinit var offenderValidator: OffenderValidator

  @Mock
  private lateinit var searchPage: SearchPage

  @Mock
  private lateinit var newOffenderPage: NewOffenderPage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Test
  fun `given offender data when create offender is called then create offender and return details`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOffenderRequest()
      val expectedCreatedSentence = CreatedSentence(sentenceId)
      val expectedCreatedOffender = CreatedOffender(
        offenderId,
        expectedCreatedSentence,
      )
      given(offenderPage.extractCreatedOffenderDetails(any())).willReturn(expectedCreatedOffender)

      // when
      val newOffender = offenderClient.createOffender(request)

      // then
      then(offenderValidator).should().validateOffenderCreationRequest(request)
      val inOrder = inOrder(searchPage, newOffenderPage, offenderPage)
      then(searchPage).should(inOrder).navigateToNewOffender()
      then(newOffenderPage).should(inOrder).verifyOn()
      then(newOffenderPage).should(inOrder).createOffender(request)
      then(newOffenderPage).should(inOrder).throwIfInvalid()
      then(offenderPage).should(inOrder).verifyOn()
      then(offenderPage).should(inOrder).updateAdditionalAddresses(request.additionalAddresses)
      then(offenderPage).should(inOrder).throwIfInvalid()
      assertEquals(expectedCreatedOffender, newOffender)
    }
  }
}
