package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender

@Component
class PpudClient() {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun searchForOffender(croNumber: String?): List<Offender> {
    log.info("Searching in PPUD Client")
    return listOf(Offender(croNumber ?: "", "John", "Teal"))
  }
}