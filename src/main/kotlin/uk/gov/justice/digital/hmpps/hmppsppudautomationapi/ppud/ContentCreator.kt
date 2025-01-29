package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.ApplicationScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress

@Component
@ApplicationScope
internal class ContentCreator {

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
