package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease.PostReleaseConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage

@Service
internal class PostReleaseClient {

  @Autowired
  private lateinit var postReleaseConfig: PostReleaseConfig

  @Autowired
  private lateinit var postReleasePage: PostReleasePage

  fun updatePostRelease(
    releaseId: String,
    custodyType: SupportedCustodyType,
    updatePostReleaseRequest: UpdatePostReleaseRequest,
  ) {
    val licenceType = custodyType.licenceType.getFullName(postReleaseConfig)

    postReleasePage.navigateToPostReleaseFor(releaseId)
    postReleasePage.updatePostRelease(updatePostReleaseRequest, licenceType)
    postReleasePage.throwIfInvalid()
  }
}
