package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

enum class DocumentCategory(val title: String) {
  PartA("Part A"),
  Licence("Licence"),
  PreviousConvictions("Pre Cons"),
  PreSentenceReport("PSR"),
  OASys("OASys"),
  ChargeSheet("Charge Sheet"),
}