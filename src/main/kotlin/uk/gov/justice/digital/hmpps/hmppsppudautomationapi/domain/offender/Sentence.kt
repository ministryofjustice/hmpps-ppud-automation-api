package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedReleasedUnder.IPP_LICENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedReleasedUnder.LIFE_LICENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.SupportedRecallType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.SupportedRecallType.DETERMINATE_RECALL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.SupportedRecallType.INDETERMINATE_RECALL
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
  val releasedUnder: SupportedReleasedUnder?,
  val recallType: SupportedRecallType,
) {
  DETERMINATE("Determinate", null, DETERMINATE_RECALL),
  EDS("EDS", null, DETERMINATE_RECALL),
  EDS_NON_PAROLE("EDS (non parole)", null, DETERMINATE_RECALL),
  IPP("IPP", IPP_LICENCE, INDETERMINATE_RECALL),
  DPP("DPP", IPP_LICENCE, INDETERMINATE_RECALL),
  MANDATORY_MLP("Mandatory (MLP)", LIFE_LICENCE, INDETERMINATE_RECALL),
  DISCRETIONARY("Discretionary", LIFE_LICENCE, INDETERMINATE_RECALL),
  DISCRETIONARY_TARIFF_EXPIRED("Discretionary (Tariff Expired)", LIFE_LICENCE, INDETERMINATE_RECALL),
  AUTOMATIC("Automatic", LIFE_LICENCE, INDETERMINATE_RECALL),
  ;

  override fun toString(): String = fullName

  companion object {
    fun forFullName(fullName: String): SupportedCustodyType = values().first { it.fullName == fullName }
  }
}
