package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.sentence

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence.SentenceClient

@Service
class SentenceService {

  @Autowired
  private lateinit var authClient: PpudAuthClient

  @Autowired
  private lateinit var sentenceClient: SentenceClient

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createSentence(offenderId: String, request: CreateOrUpdateSentenceRequest): CreatedSentence {
    log.info("Creating sentence in PPUD Client")
    return authClient.performLoggedInOperation(
      retryOnFailure = true,
    ) {
      sentenceClient.createSentence(
        offenderId, request,
      )
    }
  }

  suspend fun updateSentence(offenderId: String, sentenceId: String, request: CreateOrUpdateSentenceRequest) {
    log.info("Updating sentence in PPUD Client")
    return authClient.performLoggedInOperation(
      retryOnFailure = true,
    ) {
      sentenceClient.updateSentence(
        offenderId, sentenceId, request,
      )
    }
  }
}