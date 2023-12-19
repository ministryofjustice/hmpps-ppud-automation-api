package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CurrentDate {
  fun now(): LocalDate = LocalDate.now()
}
