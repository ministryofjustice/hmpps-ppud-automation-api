package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.recall

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.recall.RecallConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.recall.recallConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.SupportedRecallType.DETERMINATE_RECALL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.SupportedRecallType.INDETERMINATE_RECALL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release.ReleaseClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomEnum
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class RecallClientTest {

  @InjectMocks
  private lateinit var recallClient: RecallClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

  @Spy
  private val recallConfig: RecallConfig = recallConfig()

  @Mock
  private lateinit var recallPage: RecallPage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var webDriverNavigation: Navigation

  @Mock
  private lateinit var releaseClient: ReleaseClient

  @Mock
  private lateinit var sentenceClient: SentenceClient

  private val custodyTypesWithDeterminateRecallType =
    enumValues<SupportedCustodyType>().filter { it.recallType === DETERMINATE_RECALL }

  private val custodyTypesWithIndeterminateRecallType =
    enumValues<SupportedCustodyType>().filter { it.recallType === INDETERMINATE_RECALL }

  @Test
  fun `returns ID of matching Recall if one found instead of creating new Recall`() {
    runBlocking {
      // given
      setUpDriverNavigation()

      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val receivedDateTime = LocalDateTime.now()
      val recommendedTo = PpudUser(randomString("fullName"), randomString("teamName"))
      val createRecallRequest = generateCreateRecallRequest(
        receivedDateTime = receivedDateTime,
        recommendedTo = recommendedTo,
      )
      val matchingRecallLink = "/link/to/matching/recall"
      val recallId = randomPpudId()
      given(navigationTreeViewComponent.extractRecallLinks(releaseId)).willReturn(
        listOf(
          matchingRecallLink,
        ),
      )
      given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))
      given(recallPage.isMatching(receivedDateTime, recommendedTo)).willReturn(true)

      // when
      val returnedRecall = recallClient.createRecall(offenderId, releaseId, createRecallRequest)

      // then
      assertThat(returnedRecall.id).isEqualTo(recallId)
      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, recallPage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$matchingRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(navigationTreeViewComponent).should(never()).navigateToNewRecallFor(any())
      then(recallPage).should(never()).createRecall(any(), any())
      then(recallPage).should(never()).addContrabandMinuteIfNeeded(createRecallRequest)
      then(recallPage).should().extractCreatedRecallDetails()
      then(releaseClient).shouldHaveNoInteractions()
    }
  }

  @Test
  fun `creates new determinate Recall if no matching one found`() {
    runBlocking {
      val custodyType = randomEnum<SupportedCustodyType>(exclude = custodyTypesWithIndeterminateRecallType)
      testCreateRecall(custodyType, recallConfig.determinateRecallType)
    }
  }

  @Test
  fun `creates new indeterminate Recall if no matching one found`() {
    runBlocking {
      val custodyType = randomEnum<SupportedCustodyType>(exclude = custodyTypesWithDeterminateRecallType)
      testCreateRecall(custodyType, recallConfig.indeterminateRecallType)
    }
  }

  private fun testCreateRecall(
    custodyType: SupportedCustodyType,
    expectedRecallType: String,
  ): CreatedRecall {
    // given
    setUpDriverNavigation()

    val offenderId = randomPpudId()
    val releaseId = randomPpudId()
    val receivedDateTime = LocalDateTime.now()
    val recommendedTo = generatePpudUser()
    val createRecallRequest = generateCreateRecallRequest(
      receivedDateTime = receivedDateTime,
      recommendedTo = recommendedTo,
    )

    val sentenceId = randomString()
    given(releaseClient.getSentenceIdForRelease(releaseId)).willReturn(sentenceId)

    given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = custodyType.fullName))

    val nonMatchingRecallLink = "/link/to/non-matching/recall"
    val persistedRecallLink = "/link/to/persisted/recall"
    val recallId = randomPpudId()
    given(navigationTreeViewComponent.extractRecallLinks(releaseId))
      .willReturn(listOf(nonMatchingRecallLink)) // Before creation
      .willReturn(listOf(nonMatchingRecallLink, persistedRecallLink)) // After creation
    given(recallPage.extractCreatedRecallDetails()).willReturn(CreatedRecall(recallId))
    given(recallPage.isMatching(receivedDateTime, recommendedTo))
      .willReturn(false) // Before creation
      .willReturn(false) // After creation - non-matching recall
      .willReturn(true) // After creation - matching (created) recall

    // when
    val createdRecall = recallClient.createRecall(offenderId, releaseId, createRecallRequest)

    // then
    assertThat(createdRecall.id).isEqualTo(recallId)

    val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, recallPage)

    // check for existing recalls
    then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
    then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
    then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$nonMatchingRecallLink")
    then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)

    // create new recall
    then(navigationTreeViewComponent).should(inOrder).navigateToNewRecallFor(releaseId)
    then(recallPage).should(inOrder).createRecall(createRecallRequest, expectedRecallType)
    then(recallPage).should(inOrder).throwIfInvalid()
    then(recallPage).should(inOrder).addContrabandMinuteIfNeeded(createRecallRequest)

    // find created recall and extract
    then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
    then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$nonMatchingRecallLink")
    then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
    then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$persistedRecallLink")
    then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
    return then(recallPage).should(inOrder).extractCreatedRecallDetails()
  }

  @Test
  fun `throws UnsupportedCustodyTypeException if unexpected custody type encountered`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest()

      val sentenceId = randomString()
      given(releaseClient.getSentenceIdForRelease(releaseId)).willReturn(sentenceId)
      val custodyType = randomString()
      given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = custodyType))

      val expectedExceptionMessage = "Sentence $sentenceId has an unsupported custody type: $custodyType"

      // when then
      assertThatThrownBy { recallClient.createRecall(offenderId, releaseId, createRecallRequest) }
        .isInstanceOf(UnsupportedCustodyTypeException::class.java)
        .hasMessage(expectedExceptionMessage)
    }
  }

  @Test
  fun `bubbles up exceptions raised during recall creation`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val createRecallRequest = generateCreateRecallRequest()
      val exceptionMessage = randomString()
      val exception = RuntimeException(exceptionMessage)

      val sentenceId = randomString()
      given(releaseClient.getSentenceIdForRelease(releaseId)).willReturn(sentenceId)
      given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = randomEnum<SupportedCustodyType>().fullName))
      given(recallPage.throwIfInvalid()).willThrow(exception)

      // when then
      assertThatThrownBy { recallClient.createRecall(offenderId, releaseId, createRecallRequest) }
        .isSameAs(exception)

      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).createRecall(eq(createRecallRequest), any())
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  private fun setUpDriverNavigation() {
    given(driver.navigate()).willReturn(webDriverNavigation)
  }
}
