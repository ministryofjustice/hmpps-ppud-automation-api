package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.random.Random

private const val sixtyYearsInDays: Long = 21900

private const val secondsInADay: Long = 86400

fun randomString(prefix: String = "random"): String {
  return "$prefix-${UUID.randomUUID()}"
}

fun randomCroNumber(): String {
  val serial = Random.nextInt(100000, 999999)
  val year = Random.nextInt(10, 23)
  return "$serial/${year}A"
}

fun randomNomsId(): String {
  val serial = Random.nextInt(1000, 9999)
  return "A${serial}BC"
}

fun randomDate(): LocalDate {
  return LocalDate.parse("2005-01-01").minusDays(Random.nextLong(sixtyYearsInDays))
}

fun randomTimeToday(): LocalDateTime {
  return LocalDate.now().atTime(LocalTime.ofSecondOfDay(Random.nextLong(secondsInADay)))
}

fun randomPpudId(): String {
  val randomSerial = Random.nextInt(1000000, 9999999)
  return "4F${randomSerial}E64657269643D313632393134G721H665"
}

fun randomRiskOfSeriousHarmLevel(): RiskOfSeriousHarmLevel {
  val randomIndex = Random.nextInt(0, RiskOfSeriousHarmLevel.entries.count() - 1)
  return RiskOfSeriousHarmLevel.entries[randomIndex]
}

// This will create a request that is useful for mocked testing but uses random values
// so some of the values won't be acceptable to PPUD.
fun generateCreateRecallRequest(
  sentenceDate: LocalDate? = null,
  releaseDate: LocalDate? = null,
  isExtendedSentence: Boolean? = null,
  isInCustody: Boolean? = null,
  riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel? = null,
): CreateRecallRequest {
  return CreateRecallRequest(
    decisionDateTime = randomTimeToday(),
    isExtendedSentence = isExtendedSentence ?: Random.nextBoolean(),
    isInCustody = isInCustody ?: Random.nextBoolean(),
    mappaLevel = randomString("mappaLevel"),
    policeForce = randomString("policeForce"),
    probationArea = randomString("probationArea"),
    receivedDateTime = randomTimeToday(),
    recommendedToOwner = randomString("recommendedToOwner"),
    releaseDate = releaseDate ?: randomDate(),
    riskOfSeriousHarmLevel = riskOfSeriousHarmLevel ?: randomRiskOfSeriousHarmLevel(),
    sentenceDate = sentenceDate ?: randomDate(),
  )
}
