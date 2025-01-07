package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

class IsSameDayAs(private val thisDate: LocalDate, private val message: String) : Consumer<String> {

  override fun accept(that: String) {
    val thatDate = LocalDate.parse(that, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    if (!thisDate.isEqual(thatDate)) {
      throw AssertionError(message)
    }
  }
}

fun isSameDayAs(date: LocalDate, message: String): IsSameDayAs = IsSameDayAs(date, message)
