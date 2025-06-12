package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.release.ReleaseConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.release.releaseConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.UnsupportedCustodyTypeException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease.PostReleaseClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent.Companion.url
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.selenium.TreeViewNode
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomEnum
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class ReleaseClientTest {

  @InjectMocks
  private lateinit var client: ReleaseClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

  @Spy
  private val releaseConfig: ReleaseConfig = releaseConfig()

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var webDriverNavigation: Navigation

  @Mock
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Mock
  private lateinit var releasePage: ReleasePage

  @Mock
  private lateinit var offenderPage: OffenderPage

  @Mock
  private lateinit var sentenceClient: SentenceClient

  @Mock
  private lateinit var postReleaseClient: PostReleaseClient

  private val custodyTypesWithUserSetReleasedUnder =
    enumValues<SupportedCustodyType>().filter { it.releasedUnder === null }

  private val custodyTypesWithFixedReleasedUnder =
    enumValues<SupportedCustodyType>().filter { !custodyTypesWithUserSetReleasedUnder.contains(it) }

  @Test
  fun `updates matching release using provided releasedUnder when not predetermined by sentence custodyType`() {
    val custodyTypeWithUserSetReleasedUnder =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithFixedReleasedUnder)
    testUpdateRelease(custodyTypeWithUserSetReleasedUnder)
  }

  @Test
  fun `updates matching release using predetermined releasedUnder, ignoring the provided one`() {
    val custodyTypeWithFixedReleasedUnder =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithUserSetReleasedUnder)
    testUpdateRelease(custodyTypeWithFixedReleasedUnder)
  }

  @Test
  fun `creates new release using provided releasedUnder when not predetermined by sentence custodyType`() {
    val custodyTypeWithUserSetReleasedUnder =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithFixedReleasedUnder)
    testCreateNewRelease(custodyTypeWithUserSetReleasedUnder)
  }

  @Test
  fun `creates new release using predetermined releasedUnder, ignoring the provided one`() {
    val custodyTypeWithUserSetReleasedUnder =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithUserSetReleasedUnder)
    testCreateNewRelease(custodyTypeWithUserSetReleasedUnder)
  }

  @Test
  fun `throws UnsupportedCustodyTypeException if unexpected custody type encountered`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val requestReleasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, requestReleasedUnder)
      val custodyType = randomString()
      given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = custodyType))

      val expectedException =
        UnsupportedCustodyTypeException("Sentence $sentenceId has an unsupported custody type: $custodyType")

      // when then
      assertThatThrownBy { client.createOrUpdateRelease(offenderId, sentenceId, request) }
        .usingRecursiveComparison()
        .isEqualTo(expectedException)
    }
  }

  @Test
  fun `returns ID of the sentence the given release belongs to`() {
    // given
    val releaseId = randomPpudId()

    val sentenceNode: TreeViewNode = mock()
    given(navigationTreeViewComponent.findSentenceNodeForRelease(releaseId)).willReturn(sentenceNode)

    val expectedSentenceId = randomString()
    val sentenceNodeUrl = randomString() + "?data=" + expectedSentenceId
    given(sentenceNode.url).willReturn(sentenceNodeUrl)

    // when
    val actualSentenceId = client.getSentenceIdForRelease(releaseId)

    // then
    assertThat(actualSentenceId).isEqualTo(expectedSentenceId)
  }

  private fun testUpdateRelease(custodyType: SupportedCustodyType) {
    runBlocking {
      // given
      setUpDriverNavigation()

      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val requestReleasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, requestReleasedUnder)
      val expectedReleasedUnder = custodyType.releasedUnder?.getFullName(releaseConfig) ?: requestReleasedUnder
      given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = custodyType.fullName))

      val matchingReleaseLink = "/link/to/matching/release"
      val releaseId = randomPpudId()
      val expectedUpdatedRelease = CreatedOrUpdatedRelease(releaseId)
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)).willReturn(
        listOf(
          matchingReleaseLink,
        ),
      )
      given(
        releasePage.isMatching(
          releasedFrom,
          expectedReleasedUnder,
        ),
      ).willReturn(true)
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      // when
      val actualUpdatedRelease = client.createOrUpdateRelease(offenderId, sentenceId, request)

      // then
      assertThat(actualUpdatedRelease).isEqualTo(expectedUpdatedRelease)
      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$matchingReleaseLink")
      then(releasePage).should(inOrder).updateRelease()
      then(releasePage).should(inOrder).throwIfInvalid()
      then(postReleaseClient).should().updatePostRelease(releaseId, custodyType, request.postRelease)
    }
  }

  private fun testCreateNewRelease(custodyType: SupportedCustodyType) {
    runBlocking {
      // given
      setUpDriverNavigation()

      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val requestReleasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, requestReleasedUnder)
      val expectedReleasedUnder = custodyType.releasedUnder?.getFullName(releaseConfig) ?: requestReleasedUnder
      given(sentenceClient.getSentence(sentenceId)).willReturn(sentence(custodyType = custodyType.fullName))
      val releaseId = randomPpudId()
      val linkToPersistedRelease = randomString()
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease))
        .willReturn(listOf()) // Before creation
        .willReturn(listOf(linkToPersistedRelease)) // After creation
      given(releasePage.isMatching(releasedFrom, expectedReleasedUnder)).willReturn(true) // After creation
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      // when
      val updatedRelease = client.createOrUpdateRelease(offenderId, sentenceId, request)

      // then
      assertThat(updatedRelease.id).isEqualTo(releaseId)
      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(navigationTreeViewComponent).should(inOrder).navigateToNewOrEmptyReleaseFor(sentenceId)
      then(releasePage).should().createRelease(request)
      then(releasePage).should(inOrder).throwIfInvalid()
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$linkToPersistedRelease")
      then(releasePage).should(inOrder).isMatching(releasedFrom, expectedReleasedUnder)
      then(releasePage).should(inOrder).extractReleaseId()
      then(postReleaseClient).should().updatePostRelease(releaseId, custodyType, request.postRelease)
    }
  }

  private fun setUpDriverNavigation() {
    given(driver.navigate()).willReturn(webDriverNavigation)
  }
}
