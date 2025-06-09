package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.recall

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.recall.RecallClient

@Service
internal class RecallService {

  @Autowired
  private lateinit var authClient: PpudAuthClient

  @Autowired
  private lateinit var recallClient: RecallClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecall(
    offenderId: String,
    releaseId: String,
    recallRequest: CreateRecallRequest,
  ): CreatedRecall {
    log.info("Creating recall in PPUD Client")
    return authClient.performLoggedInOperation(
      retryOnFailure = true,
    ) {
      recallClient.createRecall(offenderId, releaseId, recallRequest)
    }
  }
}