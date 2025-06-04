package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

// TODO - are these names the same in both dev and pre-prod? If not, we'll need to define app variables
//        for them (as we already do with e.g. PPUD_OFFENDER_CASEWORKER_UAL) and link them to the enum
enum class ReleasedUnder(val fullName: String) {
  IPP_LICENCE("IPP Licence [*]"),
  LIFE_LICENCE("Life Licence [*]"),
}
