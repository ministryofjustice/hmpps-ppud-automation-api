package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.DETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.INDETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest

@Component
class SentenceComparator {

  fun areMatching(existing: Sentence, request: CreateOrUpdateSentenceRequest): Boolean =
    areMatchingDeterminateSentences(existing, request) ||
      areMatchingIndeterminateSentences(existing, request)

  private fun areMatchingDeterminateSentences(existing: Sentence, request: CreateOrUpdateSentenceRequest): Boolean {
    try {
      val custodyType = SupportedCustodyType.forFullName(existing.custodyType)
      if (custodyType.custodyGroup !== DETERMINATE) {
        return false
      }
    } catch (e: UnsupportedOperationException) {
      return false
    }

    return existing.custodyType == request.custodyType &&
      existing.dateOfSentence == request.dateOfSentence &&
      existing.sentencingCourt == request.sentencingCourt &&
      existing.mappaLevel == request.mappaLevel &&
      existing.sentenceLength?.partYears == request.sentenceLength?.partYears &&
      existing.sentenceLength?.partMonths == request.sentenceLength?.partMonths &&
      existing.sentenceLength?.partDays == request.sentenceLength?.partDays &&
      existing.licenceExpiryDate == request.licenceExpiryDate &&
      existing.sentenceExpiryDate == request.sentenceExpiryDate &&
      existing.sentencedUnder == request.sentencedUnder
  }

  private fun areMatchingIndeterminateSentences(existing: Sentence, request: CreateOrUpdateSentenceRequest): Boolean {
    try {
      val custodyType = SupportedCustodyType.forFullName(existing.custodyType)
      if (custodyType.custodyGroup !== INDETERMINATE) {
        return false
      }
    } catch (e: UnsupportedOperationException) {
      return false
    }

    return existing.custodyType == request.custodyType &&
      existing.dateOfSentence == request.dateOfSentence &&
      existing.sentencingCourt == request.sentencingCourt
  }
}
