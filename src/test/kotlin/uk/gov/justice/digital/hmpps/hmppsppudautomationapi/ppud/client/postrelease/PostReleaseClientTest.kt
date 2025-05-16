package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.updatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class PostReleaseClientTest {

  @InjectMocks
  private lateinit var client: PostReleaseClient

  @Mock
  private lateinit var postReleasePage: PostReleasePage

  @Test
  fun `update post release`() {
    // given
    val releaseId = randomString()
    val updatePostReleaseRequest = updatePostReleaseRequest()

    // when
    client.updatePostRelease(releaseId, updatePostReleaseRequest)

    // then
    then(postReleasePage).should().navigateToPostReleaseFor(releaseId)
    then(postReleasePage).should().updatePostRelease(updatePostReleaseRequest)
    then(postReleasePage).should().throwIfInvalid()
  }
}
