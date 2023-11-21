package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.ApplicationScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest

@Component
@ApplicationScope
internal class ContentCreator {

  fun generateMinuteText(createRecallRequest: CreateRecallRequest): String {
    val extended = if (createRecallRequest.isExtendedSentence) {
      "YES"
    } else {
      "NO"
    }
    val custody = if (createRecallRequest.isInCustody) {
      "YES at HMP"
    } else {
      "NO"
    }
    return "BACKGROUND INFO ${System.lineSeparator()}" +
      "Extended sentence: $extended${System.lineSeparator()}" +
      "Risk of Serious Harm Level: ${createRecallRequest.riskOfSeriousHarmLevel.descriptor.uppercase()}${System.lineSeparator()}" +
      "In custody: $custody"
  }
}
