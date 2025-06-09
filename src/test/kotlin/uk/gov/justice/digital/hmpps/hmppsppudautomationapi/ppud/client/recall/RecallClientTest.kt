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
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.RecallPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class RecallClientTest {

  @InjectMocks
  private lateinit var recallClient: RecallClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

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
  private lateinit var sentenceClient: SentenceClient

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
      then(recallPage).should(never()).createRecall(any())
      then(recallPage).should(never()).addContrabandMinuteIfNeeded(createRecallRequest)
      then(recallPage).should().extractCreatedRecallDetails()
    }
  }

  @Test
  fun `creates new Recall if no matching one found`() {
    runBlocking {
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
      then(recallPage).should(inOrder).createRecall(createRecallRequest)
      then(recallPage).should(inOrder).throwIfInvalid()
      then(recallPage).should(inOrder).addContrabandMinuteIfNeeded(createRecallRequest)

      // find created recall and extract
      then(navigationTreeViewComponent).should(inOrder).extractRecallLinks(releaseId)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$nonMatchingRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$persistedRecallLink")
      then(recallPage).should(inOrder).isMatching(receivedDateTime, recommendedTo)
      then(recallPage).should(inOrder).extractCreatedRecallDetails()
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
      given(recallPage.throwIfInvalid()).willThrow(exception)

      // when then
      assertThatThrownBy { recallClient.createRecall(offenderId, releaseId, createRecallRequest) }
        .usingRecursiveComparison()
        .isEqualTo(exception)

      val inOrder = inOrder(recallPage)
      then(recallPage).should(inOrder).createRecall(createRecallRequest)
      then(recallPage).should(inOrder).throwIfInvalid()
    }
  }

  private fun setUpDriverNavigation() {
    given(driver.navigate()).willReturn(webDriverNavigation)
  }

}