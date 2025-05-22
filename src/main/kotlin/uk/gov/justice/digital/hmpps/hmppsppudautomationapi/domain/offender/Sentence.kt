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
// TODO The plan here is to have the different values that should be fixed for each custody type
//  as listed in MRD-2684, so that we can e.g. in the Release case:
//     1. Get the Sentence using the offenderId & sentenceId
//     2. Check out the custody type on the sentence and pass it through the forFullName function below
//     3. With the SupportedCustodyType in hand, pick out the releasedUnder value and apply (or apply the
//        one provided by the caller if the one coming from the enum is null, as in the determinate cases)
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


  override fun toString(): String {
    return fullName
  }

  companion object {
    fun forFullName(fullName: String): SupportedCustodyType {
      return values().first { it.fullName == fullName }
    }
  }
}