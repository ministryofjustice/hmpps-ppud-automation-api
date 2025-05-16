package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease.PostReleaseClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class ReleaseClientTest {

  @InjectMocks
  private lateinit var client: ReleaseClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

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
  private lateinit var postReleaseClient: PostReleaseClient

  @BeforeEach
  fun setUpDriverNavigation() {
    given(driver.navigate()).willReturn(webDriverNavigation)
  }

  @Test
  fun `given offender ID and sentence ID and release data for matching release when create or update release is called then update matching release`() {
    runBlocking {
      // given
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val releasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, releasedUnder)
      val matchingReleaseLink = "/link/to/matching/release"
      val releaseId = randomPpudId()
      val expectedUpdatedRelease = CreatedOrUpdatedRelease(releaseId)
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease)).willReturn(
        listOf(
          matchingReleaseLink,
        ),
      )
      given(releasePage.isMatching(releasedFrom, releasedUnder)).willReturn(true)
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
      then(postReleaseClient).should().updatePostRelease(releaseId, request.postRelease)
    }
  }

  @Test
  fun `given offender ID and sentence ID and release data for new release when create or update release is called then create new release and return ID from persisted release`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val dateOfRelease = randomDate()
      val releasedFrom = randomString("releasedFrom")
      val releasedUnder = randomString("releasedUnder")
      val request = generateCreateOrUpdateReleaseRequest(dateOfRelease, releasedFrom, releasedUnder)
      val releaseId = randomPpudId()
      val linkToPersistedRelease = randomString()
      given(navigationTreeViewComponent.extractReleaseLinks(sentenceId, dateOfRelease))
        .willReturn(listOf()) // Before creation
        .willReturn(listOf(linkToPersistedRelease)) // After creation
      given(releasePage.isMatching(releasedFrom, releasedUnder)).willReturn(true) // After creation
      given(releasePage.extractReleaseId()).willReturn(releaseId)

      val updatedRelease = client.createOrUpdateRelease(offenderId, sentenceId, request)

      val inOrder = inOrder(offenderPage, navigationTreeViewComponent, webDriverNavigation, releasePage)
      then(offenderPage).should(inOrder).viewOffenderWithId(offenderId)
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(navigationTreeViewComponent).should(inOrder).navigateToNewOrEmptyReleaseFor(sentenceId)
      then(releasePage).should().createRelease(request)
      then(releasePage).should(inOrder).throwIfInvalid()
      then(navigationTreeViewComponent).should(inOrder).extractReleaseLinks(sentenceId, dateOfRelease)
      then(webDriverNavigation).should(inOrder).to("${ppudClientConfig.url}$linkToPersistedRelease")
      then(releasePage).should(inOrder).isMatching(releasedFrom, releasedUnder)
      then(releasePage).should(inOrder).extractReleaseId()
      then(postReleaseClient).should().updatePostRelease(releaseId, request.postRelease)
      assertEquals(releaseId, updatedRelease?.id)
    }
  }

}