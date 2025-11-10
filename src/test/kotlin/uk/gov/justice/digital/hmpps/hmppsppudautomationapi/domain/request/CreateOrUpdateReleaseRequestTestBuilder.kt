package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate

internal fun createOrUpdateReleaseRequest(
  dateOfRelease: LocalDate = randomDate(),
  postRelease: UpdatePostReleaseRequest = updatePostReleaseRequest(),
  releasedFrom: String = randomString(),
  releasedUnder: String = randomString(),
) = CreateOrUpdateReleaseRequest(dateOfRelease, postRelease, releasedFrom, releasedUnder)
