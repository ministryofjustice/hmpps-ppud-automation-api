package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.ApplicationScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest

@Component
@ApplicationScope
internal class ContentCreator {

  fun generateRecallMinuteText(createRecallRequest: CreateRecallRequest): String {
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

  fun addAdditionalAddressesToComments(additionalAddresses: List<OffenderAddress>, currentComments: String): String {
    val formattedAddresses = additionalAddresses.map {
      listOf(it.premises, it.line1, it.line2, it.postcode, it.phoneNumber)
        .filter { line -> line.isNotBlank() }
        .joinToString(separator = ", ")
    }.filter { it.isNotBlank() }

    val prefix = if (formattedAddresses.size == 1) "Additional address" else "Additional addresses"
    val addressBlock = if (formattedAddresses.any()) {
      formattedAddresses.joinToString(prefix = "$prefix:${System.lineSeparator()}", separator = System.lineSeparator())
    } else {
      ""
    }

    return listOf(addressBlock, currentComments)
      .filter { it.isNotBlank() }
      .joinToString(separator = "${System.lineSeparator()}${System.lineSeparator()}")
  }
}
