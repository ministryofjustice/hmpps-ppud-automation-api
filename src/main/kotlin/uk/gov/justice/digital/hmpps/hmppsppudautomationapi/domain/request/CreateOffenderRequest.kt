package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import java.time.LocalDate

class CreateOffenderRequest(
  val address: OffenderAddress = OffenderAddress(),
  val additionalAddresses: List<OffenderAddress> = emptyList(),
  croNumber: String? = null,
  @field:NotBlank
  @field:Pattern(regexp = "Determinate")
  val custodyType: String,
  val dateOfBirth: LocalDate,
  val dateOfSentence: LocalDate,
  @field:NotBlank
  val ethnicity: String,
  @field:NotBlank
  val firstNames: String,
  @field:NotBlank
  val familyName: String,
  @field:NotBlank
  val gender: String,
  @field:NotBlank
  val indexOffence: String,
  val isInCustody: Boolean,
  @field:NotBlank
  val mappaLevel: String,
  nomsId: String? = null,
  @field:NotBlank
  val prisonNumber: String,
) {
  val croNumber: String = croNumber ?: ""
  val nomsId: String = nomsId ?: ""
}
