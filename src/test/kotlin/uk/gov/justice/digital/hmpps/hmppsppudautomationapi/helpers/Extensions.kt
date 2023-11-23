package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import java.time.LocalDateTime

fun LocalDateTime.withoutSeconds(): String {
  return this
    .truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
