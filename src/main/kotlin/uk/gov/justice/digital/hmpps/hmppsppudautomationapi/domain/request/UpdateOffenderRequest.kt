package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import java.time.LocalDate

class UpdateOffenderRequest(
  val address: OffenderAddress = OffenderAddress(),
  val additionalAddresses: List<OffenderAddress> = emptyList(),
  croNumber: String? = null,
  val dateOfBirth: LocalDate,
  @field:NotBlank
  val ethnicity: String,
  @field:NotBlank
  val familyName: String,
  @field:NotBlank
  val firstNames: String,
  @field:NotBlank
  val gender: String,
  val isInCustody: Boolean,
  nomsId: String? = null,
  @field:NotBlank
  val prisonNumber: String,
) {
  val croNumber: String = croNumber ?: ""
  val nomsId: String = nomsId ?: ""
}
