package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class ContentCreatorTest {

  @ParameterizedTest
  @CsvSource(
    "true,YES",
    "false,NO",
  )
  fun `given extended sentence indicator when generateMinuteText is called then text contains extended sentence yes or no`(
    isExtendedSentence: Boolean,
    expected: String,
  ) {
    val creator = ContentCreator()
    val request = generateCreateRecallRequest(isExtendedSentence = isExtendedSentence)
    val result = creator.generateRecallMinuteText(request)
    assertTrue(result.contains("Extended sentence: $expected"), "Text was:\n$result")
  }

  @ParameterizedTest
  @CsvSource(
    "Low,LOW",
    "Medium,MEDIUM",
    "High,HIGH",
    "VeryHigh,VERY HIGH",
    "NotApplicable,NOT APPLICABLE",
  )
  fun `given risk of serious harm level when generateMinuteText is called then text contains level`(
    riskOfSeriousHarmLevel: String,
    expected: String,
  ) {
    val creator = ContentCreator()
    val request =
      generateCreateRecallRequest(riskOfSeriousHarmLevel = RiskOfSeriousHarmLevel.valueOf(riskOfSeriousHarmLevel))
    val result = creator.generateRecallMinuteText(request)
    assertTrue(result.contains("Risk of Serious Harm Level: $expected"), "Text was:\n$result")
  }

  @ParameterizedTest
  @CsvSource(
    "true,YES at HMP",
    "false,NO",
  )
  fun `given indicator of offender in custody when generateMinuteText is called then text contains custody status`(
    isInCustody: Boolean,
    expected: String,
  ) {
    val creator = ContentCreator()
    val request = generateCreateRecallRequest(isInCustody = isInCustody)
    val result = creator.generateRecallMinuteText(request)
    assertTrue(result.contains("In custody: $expected"), "Text was:\n$result")
  }

  @ParameterizedTest
  @CsvSource(
    "premises,line1,line2,postcode,phone,'premises, line1, line2, postcode, phone'",
    "premises,line1,line2,postcode,'','premises, line1, line2, postcode'",
    "premises,line1,line2,'','','premises, line1, line2'",
    "premises,line1,'','','','premises, line1'",
    "premises,'','','','','premises'",
    "premises,line1,'',postcode,phone,'premises, line1, postcode, phone'",
    "premises,line1,'','',phone,'premises, line1, phone'",
    "premises,'','','',phone,'premises, phone'",
  )
  fun `given additional address when addAdditionalAddressesToComments is called then address is formatted on one line`(
    premises: String,
    line1: String,
    line2: String,
    postcode: String,
    phoneNumber: String,
    expectedLine: String,
  ) {
    val creator = ContentCreator()
    val addresses = listOf(OffenderAddress(premises, line1, line2, postcode, phoneNumber))
    val result = creator.addAdditionalAddressesToComments(addresses, "")

    val expected =
      "Additional address:${System.lineSeparator()}" +
        expectedLine
    assertEquals(expected, result)
  }

  @Test
  fun `given multiple additional addresses when addAdditionalAddressesToComments is called then addresses are added on separate lines`() {
    val creator = ContentCreator()
    val addresses = listOf(
      OffenderAddress("first premises"),
      OffenderAddress("second premises"),
    )

    val result = creator.addAdditionalAddressesToComments(addresses, "")

    val expected =
      "Additional addresses:${System.lineSeparator()}" +
        "first premises${System.lineSeparator()}" +
        "second premises"
    assertEquals(expected, result)
  }

  @Test
  fun `given additional address and existing comments when addAdditionalAddressesToComments is called then addresses are prepended to comments`() {
    val creator = ContentCreator()
    val addresses = listOf(
      OffenderAddress("premises"),
    )
    val comments = randomString("comments")

    val result = creator.addAdditionalAddressesToComments(addresses, comments)

    val expected =
      "Additional address:${System.lineSeparator()}" +
        "premises${System.lineSeparator()}" +
        System.lineSeparator() +
        comments
    assertEquals(expected, result)
  }

  @Test
  fun `given no additional addresses and existing comments when addAdditionalAddressesToComments is called then return comments`() {
    val creator = ContentCreator()
    val addresses = emptyList<OffenderAddress>()
    val comments = randomString("comments")

    val result = creator.addAdditionalAddressesToComments(addresses, comments)

    assertEquals(comments, result)
  }

  @Test
  fun `given empty additional address and existing comments when addAdditionalAddressesToComments is called then return comments`() {
    val creator = ContentCreator()
    val addresses = listOf(OffenderAddress())
    val comments = randomString("comments")

    val result = creator.addAdditionalAddressesToComments(addresses, comments)

    assertEquals(comments, result)
  }
}
