package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage

@Service
internal class PostReleaseClient {

  @Autowired
  private lateinit var postReleasePage: PostReleasePage

  fun updatePostRelease(
    releaseId: String,
    updatePostReleaseRequest: UpdatePostReleaseRequest,
  ) {
    postReleasePage.navigateToPostReleaseFor(releaseId)
    postReleasePage.updatePostRelease(updatePostReleaseRequest)
    postReleasePage.throwIfInvalid()
  }
}