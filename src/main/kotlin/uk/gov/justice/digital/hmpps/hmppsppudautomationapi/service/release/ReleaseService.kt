package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.release

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release.ReleaseClient

@Service
internal class ReleaseService {

  @Autowired
  private lateinit var authClient: PpudAuthClient

  @Autowired
  private lateinit var releaseClient: ReleaseClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    createOrUpdateReleaseRequest: CreateOrUpdateReleaseRequest,
  ): CreatedOrUpdatedRelease {
    log.info("Creating/updating release in PPUD Client")
    return authClient.performLoggedInOperation(
      retryOnFailure = true,
    ) {
      releaseClient.createOrUpdateRelease(offenderId, sentenceId, createOrUpdateReleaseRequest)
    }
  }
}
