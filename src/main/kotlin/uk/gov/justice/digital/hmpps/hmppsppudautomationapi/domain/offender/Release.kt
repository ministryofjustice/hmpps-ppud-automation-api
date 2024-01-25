package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import java.time.LocalDate

data class Release(
  val category: String,
  val dateOfRelease: LocalDate,
  val postRelease: PostRelease = PostRelease(),
  val releasedFrom: String,
  val releasedUnder: String,
  val releaseType: String,
)
