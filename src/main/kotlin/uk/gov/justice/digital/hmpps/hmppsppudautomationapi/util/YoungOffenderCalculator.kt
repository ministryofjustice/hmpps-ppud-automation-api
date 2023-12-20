package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
internal class YoungOffenderCalculator(private val currentDate: CurrentDate) {

  private val youngOffenderAgeLimit: Long = 21

  fun isYoungOffender(dateOfBirth: LocalDate): Boolean {
    return currentDate.now() < dateOfBirth.plusYears(youngOffenderAgeLimit)
  }
}
