package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.INDETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.ReleasedUnder.IPP_LICENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.ReleasedUnder.LIFE_LICENCE
import java.time.LocalDate

data class Sentence(
  val id: String,
  val dateOfSentence: LocalDate,
  val custodyType: String,
  val espCustodialPeriod: EspPeriod? = null,
  val espExtendedPeriod: EspPeriod? = null,
  val licenceExpiryDate: LocalDate? = null,
  val mappaLevel: String? = null,
  val offence: Offence = Offence(),
  val releaseDate: LocalDate? = null,
  val sentenceExpiryDate: LocalDate? = null,
  val tariffExpiryDate: LocalDate? = null,
  val sentencedUnder: String? = null,
  val sentenceLength: SentenceLength? = null,
  val sentencingCourt: String,
)

/**
 * This enum refers to the custody types supported by CaR in terms of creating and
 * updating sentences. There are more types in PPUD which are ignored in one way
 * or another (if not directly in this service, in the API or UI services).
 */
enum class SupportedCustodyType(
  val fullName: String,
  val custodyGroup: CustodyGroup,
  val releasedUnder: ReleasedUnder?,
) {
  DETERMINATE("Determinate", CustodyGroup.DETERMINATE, null),
  EDS("EDS", CustodyGroup.DETERMINATE, null),
  EDS_NON_PAROLE("EDS (non parole)", CustodyGroup.DETERMINATE, null),
  IPP("IPP", INDETERMINATE, IPP_LICENCE),
  DPP("DPP", INDETERMINATE, IPP_LICENCE),
  MANDATORY_MLP("Mandatory (MLP)", INDETERMINATE, LIFE_LICENCE),
  DISCRETIONARY("Discretionary", INDETERMINATE, LIFE_LICENCE),
  DISCRETIONARY_TARIFF_EXPIRED("Discretionary (Tariff Expired)", INDETERMINATE, LIFE_LICENCE),
  AUTOMATIC("Automatic", INDETERMINATE, LIFE_LICENCE),
  ;

  override fun toString(): String = fullName

  companion object {
    fun forFullName(fullName: String): SupportedCustodyType = entries.first { it.fullName == fullName }
  }
}

enum class CustodyGroup(val fullName: String) {
  DETERMINATE("Determinate"),
  INDETERMINATE("Indeterminate"),
}
