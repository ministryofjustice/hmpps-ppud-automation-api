package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

class IsSameDayAs(private val thisDate: LocalDate) : Consumer<String> {

  var isSameDay: Boolean = false
  override fun accept(that: String) {
    val thatDate = LocalDate.parse(that, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    isSameDay = thisDate.isEqual(thatDate)
  }
}
