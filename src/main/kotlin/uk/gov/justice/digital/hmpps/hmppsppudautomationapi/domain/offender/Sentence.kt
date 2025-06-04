package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

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
enum class SupportedCustodyType(val fullName: String, val releasedUnder: ReleasedUnder?) {
  DETERMINATE("Determinate", null),
  EDS("EDS", null),
  EDS_NON_PAROLE("EDS (non parole)", null),
  IPP("IPP", IPP_LICENCE),
  DPP("DPP", IPP_LICENCE),
  MANDATORY_MLP("Mandatory (MLP)", LIFE_LICENCE),
  DISCRETIONARY("Discretionary", LIFE_LICENCE),
  DISCRETIONARY_TARIFF_EXPIRED("Discretionary (Tariff Expired)", LIFE_LICENCE),
  AUTOMATIC("Automatic", LIFE_LICENCE),
  ;

  override fun toString(): String = fullName

  companion object {
    fun forFullName(fullName: String): SupportedCustodyType = values().first { it.fullName == fullName }
  }
}
