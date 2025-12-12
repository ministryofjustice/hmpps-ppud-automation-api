package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offender.OffenderClient

@Service
class OffenderService {
  @Autowired
  private lateinit var authClient: PpudAuthClient

  @Autowired
  private lateinit var offenderClient: OffenderClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createOffender(createOffenderRequest: CreateOffenderRequest): CreatedOffender {
    log.info("Creating offender")
    return authClient.performLoggedInOperation(retryOnFailure = false) {
      offenderClient.createOffender(createOffenderRequest)
    }
  }
}
