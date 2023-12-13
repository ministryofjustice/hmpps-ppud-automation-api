package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest

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
    val result = creator.generateMinuteText(request)
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
    val result = creator.generateMinuteText(request)
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
    val result = creator.generateMinuteText(request)
    assertTrue(result.contains("In custody: $expected"), "Text was:\n$result")
  }
}
