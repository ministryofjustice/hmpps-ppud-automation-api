package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender
import java.time.LocalDate

internal class PpudClient(private val ppudUrl: String) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun searchForOffender(
    croNumber: String?,
    nomsId: String?,
    familyName: String?,
    dateOfBirth: LocalDate?,
  ): List<Offender> {
    log.info("Searching in PPUD Client")
    return listOf(
      Offender(
        "1",
        croNumber ?: "",
        nomsId ?: "",
        "John",
        familyName ?: "Teal",
        dateOfBirth ?: LocalDate.now(),
      ),
    )
  }
}
