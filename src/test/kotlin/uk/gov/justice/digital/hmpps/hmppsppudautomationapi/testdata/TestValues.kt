package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.CreateRecallRequest
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

fun randomCreateRecallRequest(
  sentenceDate: LocalDate? = null,
  releaseDate: LocalDate? = null,
): CreateRecallRequest {
  return CreateRecallRequest(
    sentenceDate = sentenceDate ?: randomDate(),
    releaseDate = releaseDate ?: randomDate(),
    recommendedToOwner = randomString("recommendedToOwner"),
    probationArea = randomString("probationArea"),
    isInCustody = Random.nextBoolean(),
    decisionDateTime = randomTimeToday(),
    receivedDateTime = randomTimeToday(),
    policeForce = randomString("policeForce"),
  )
}
