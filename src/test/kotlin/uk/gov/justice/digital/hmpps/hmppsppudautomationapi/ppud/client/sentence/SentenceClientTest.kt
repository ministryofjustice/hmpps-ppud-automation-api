package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offence.OffenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.BaseSentencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.sentence.validation.SentenceValidator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class SentenceClientTest {

  @InjectMocks
  private lateinit var sentenceClient: SentenceClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

  @Mock
  private lateinit var sentenceValidator: SentenceValidator

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var webDriverNavigation: Navigation

  @Mock
  private lateinit var sentencePageFactory: SentencePageFactory

  @Mock
  private lateinit var offenceClient: OffenceClient

  @Mock
  private lateinit var sentencePage: BaseSentencePage

  @Test
  fun `given sentence data when create sentence is called then create sentence and return ID`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val custodyType = randomString("custodyType")
      val request = generateCreateOrUpdateSentenceRequest(
        custodyType = custodyType,
      )
      val sentenceId = randomPpudId()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)
      given(sentencePage.extractCreatedSentenceDetails()).willReturn(CreatedSentence(sentenceId))

      // when
      val newSentence = sentenceClient.createSentence(offenderId, request)

      // then
      then(sentenceValidator).should().validateSentenceCreationRequest(request)
      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).navigateToNewSentence()
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).selectCustodyType(custodyType)
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).createSentence(request)
      then(sentencePage).should(inOrder).throwIfInvalid()
      then(sentencePage).should(inOrder).extractCreatedSentenceDetails()
      assertEquals(sentenceId, newSentence.id)
    }
  }

  @Test
  fun `given duplicate sentence data when create sentence is called then do not create sentence and return existing ID`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val dateOfSentence = randomDate()
      val custodyType = randomString("custodyType")
      val request = generateCreateOrUpdateSentenceRequest(
        dateOfSentence = dateOfSentence,
        custodyType = custodyType,
      )
      val sentenceId = randomPpudId()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)
      given(navigationTreeViewComponent.extractSentenceLinks(dateOfSentence, custodyType)).willReturn(listOf("/link"))
      given(driver.navigate()).willReturn(webDriverNavigation)
      given(sentencePage.isMatching(request)).willReturn(true)
      given(sentencePage.extractCreatedSentenceDetails()).willReturn(CreatedSentence(sentenceId))

      // when
      val returnedSentence = sentenceClient.createSentence(offenderId, request)

      // then
      then(sentenceValidator).should().validateSentenceCreationRequest(request)
      val inOrder =
        inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractSentenceLinks(dateOfSentence, custodyType)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}/link")
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).isMatching(request)
      then(sentencePage).should(inOrder).extractCreatedSentenceDetails()
      then(navigationTreeViewComponent).should(never()).navigateToNewSentence()
      then(sentencePage).should(never()).createSentence(request)
      then(sentencePage).should(never()).throwIfInvalid()
      assertEquals(sentenceId, returnedSentence.id)
    }
  }

  @Test
  fun `given offender ID and sentence ID and sentence data when update sentence is called then update sentence`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      // when
      sentenceClient.updateSentence(offenderId, sentenceId, request)

      // then
      then(sentenceValidator).should().validateSentenceUpdateRequest(request)
      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, sentencePageFactory, sentencePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).navigateToSentenceFor(sentenceId)
      then(sentencePageFactory).should(inOrder).sentencePage()
      then(sentencePage).should(inOrder).updateSentence(request)
      then(sentencePage).should(inOrder).throwIfInvalid()
    }
  }

  @Test
  fun `given offender ID and sentence ID return the sentence details`() {
    runBlocking {
      // given
      val sentenceId = randomPpudId()
      given(sentencePageFactory.sentencePage()).willReturn(sentencePage)

      val expectedSentence = sentence()
      given(sentencePage.extractSentenceDetails(any<(String) -> Offence>())).willReturn(expectedSentence)

      // when
      val actualSentence = sentenceClient.getSentence(sentenceId)

      // then
      assertThat(actualSentence).isEqualTo(expectedSentence)

      val inOrder = inOrder(navigationTreeViewComponent, sentencePageFactory, sentencePage)
      then(navigationTreeViewComponent).should(inOrder).navigateToSentenceFor(sentenceId)
      then(sentencePageFactory).should(inOrder).sentencePage()
      val methodCaptor = argumentCaptor<(String) -> Offence>()
      then(sentencePage).should(inOrder).extractSentenceDetails(methodCaptor.capture())
      then(offenceClient).shouldHaveNoInteractions()

      val method = methodCaptor.firstValue
      val offenceUrl = randomString()
      method.invoke(offenceUrl)
      then(offenceClient).should().getOffence(offenceUrl)
    }
  }
}
