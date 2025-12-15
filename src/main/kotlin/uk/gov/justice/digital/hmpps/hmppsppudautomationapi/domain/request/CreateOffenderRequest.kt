package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import java.time.LocalDate

class CreateOffenderRequest(
  val address: OffenderAddress = OffenderAddress(),
  val additionalAddresses: List<OffenderAddress> = emptyList(),
  croNumber: String? = null,
  val custodyType: String,
  val dateOfBirth: LocalDate,
  val dateOfSentence: LocalDate,
  val ethnicity: String,
  val firstNames: String,
  val familyName: String,
  val gender: String,
  val indexOffence: String,
  val isInCustody: Boolean,
  val mappaLevel: String,
  nomsId: String? = null,
  val prisonNumber: String,
  val establishment: String,
) {
  val croNumber: String = croNumber ?: ""
  val nomsId: String = nomsId ?: ""
}
