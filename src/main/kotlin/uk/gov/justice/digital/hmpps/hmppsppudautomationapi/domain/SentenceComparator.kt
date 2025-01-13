package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest

@Component
class SentenceComparator {
  fun areMatching(existing: Sentence, request: CreateOrUpdateSentenceRequest): Boolean = existing.dateOfSentence == request.dateOfSentence &&
    existing.custodyType == request.custodyType &&
    existing.sentencingCourt == request.sentencingCourt &&
    existing.mappaLevel == request.mappaLevel &&
    existing.sentenceLength?.partYears == request.sentenceLength?.partYears &&
    existing.sentenceLength?.partMonths == request.sentenceLength?.partMonths &&
    existing.sentenceLength?.partDays == request.sentenceLength?.partDays &&
    existing.licenceExpiryDate == request.licenceExpiryDate &&
    existing.sentenceExpiryDate == request.sentenceExpiryDate &&
    existing.sentencedUnder == request.sentencedUnder
}
