package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal class PpudClientFactory(@Value("\${ppud.url}") private val ppudUrl: String) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun create(): PpudClient {
    log.trace("Creating PPUD Client")

    return PpudClient(ppudUrl)
  }
}
