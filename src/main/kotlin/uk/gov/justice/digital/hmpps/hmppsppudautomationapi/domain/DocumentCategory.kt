package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

enum class DocumentCategory(val title: String, documentType: DocumentType) {
  EmailFromProbation("Recall Request Email", DocumentType.Email),
  PartA("Part A", DocumentType.Document),
  Licence("Licence", DocumentType.Document),
  PreviousConvictions("Pre Cons", DocumentType.Document),
  PreSentenceReport("PSR", DocumentType.Document),
  OASys("OASys", DocumentType.Document),
  ChargeSheet("Charge Sheet", DocumentType.Document),
  Other("Other", DocumentType.Document)
}
