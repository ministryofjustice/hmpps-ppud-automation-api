package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence

internal interface SentencePage {
  fun extractSentenceDetails(): Sentence
}
