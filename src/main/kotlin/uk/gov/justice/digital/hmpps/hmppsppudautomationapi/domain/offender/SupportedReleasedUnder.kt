package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

// TODO - are these names the same in both dev and pre-prod? If not, we'll need to define app variables
//        for them (as we already do with e.g. PPUD_OFFENDER_CASEWORKER_UAL) and link them to the enum
/**
 * This enum refers to the "Released Under" values supported by CaR in terms of
 * creating and updating releases. There are more values in PPUD which are
 * ignored/exclude  in one way or another (if not directly in this service, in
 * the API or UI services).
 */
enum class SupportedReleasedUnder(val fullName: String) {
  IPP_LICENCE("IPP Licence [*]"),
  LIFE_LICENCE("Life Licence [*]"),
}
