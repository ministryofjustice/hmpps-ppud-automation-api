package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import io.swagger.v3.oas.annotations.Hidden
import net.minidev.json.annotate.JsonIgnore
import java.time.LocalDate

data class OffenderSearchRequest(
  val croNumber: String?,
  val nomsId: String?,
  val familyName: String?,
  val dateOfBirth: LocalDate?,
) {
  @get:JsonIgnore
  @get:Hidden
  internal val containsCriteria: Boolean
    get() {
      return !croNumber.isNullOrBlank() ||
        !nomsId.isNullOrBlank() ||
        (!familyName.isNullOrBlank() && dateOfBirth != null)
    }
}
