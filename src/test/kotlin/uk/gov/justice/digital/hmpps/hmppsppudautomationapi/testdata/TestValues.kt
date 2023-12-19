package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.random.Random

const val ppudValidCustodyType = "Determinate"

const val ppudValidEthnicity = "Chinese"

const val ppudValidGender = "M"

const val ppudValidIndexOffence = "ADMINISTER DRUGS"

// Watch out for the different hyphens in the dropdown options
const val ppudValidMappaLevel = "Level 2 – Local Inter-Agency Management"

const val ppudValidPoliceForce = "Kent Police"

const val ppudValidProbationArea = "Merseyside"

const val ppudValidUserFullNameAndTeam = "Consider a Recall Test(Recall 1)"

const val ppudValidUserFullName = "Consider a Recall Test"

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

fun randomPncNumber(): String {
  val year = Random.nextInt(10, 20)
  val serial = Random.nextInt(1000000, 9999999)
  return "$year/${serial}Z"
}

fun randomPrisonNumber(): String {
  val serial = Random.nextInt(1000, 9999)
  return "AB$serial"
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

fun randomLookupName(exclude: List<LookupName> = listOf()): LookupName {
  return LookupName.entries.filter { exclude.contains(it).not() }.shuffled().first()
}

fun randomRiskOfSeriousHarmLevel(): RiskOfSeriousHarmLevel {
  val randomIndex = Random.nextInt(0, RiskOfSeriousHarmLevel.entries.count() - 1)
  return RiskOfSeriousHarmLevel.entries[randomIndex]
}

// This will create a request that is useful for mocked testing but uses random values
// so some of the values won't be acceptable to PPUD.
fun generateCreateOffenderRequest(): CreateOffenderRequest {
  return CreateOffenderRequest(
    croNumber = randomCroNumber(),
    custodyType = randomString("custodyType"),
    dateOfBirth = randomDate(),
    dateOfSentence = randomDate(),
    ethnicity = randomString("ethnicity"),
    firstNames = randomString("firstNames"),
    familyName = randomString("familyName"),
    gender = randomString("gender"),
    indexOffence = randomString("indexOffence"),
    mappaLevel = randomString("mappaLevel"),
    nomsId = randomNomsId(),
    pncNumber = randomPncNumber(),
    prisonNumber = randomPrisonNumber(),
  )
}

fun generateOffender(id: String = randomPpudId()): Offender {
  val croOtherNumber = randomCroNumber()
  return Offender(
    id = id,
    croOtherNumber = croOtherNumber,
    dateOfBirth = randomDate(),
    ethnicity = randomString("ethnicity"),
    familyName = randomString("familyName"),
    firstNames = randomString("firstNames"),
    nomsId = randomNomsId(),
    prisonNumber = randomPrisonNumber(),
    gender = randomString("gender"),
  )
}

fun generateSearchResultOffender(
  id: String? = null,
  croOtherNumber: String? = null,
  nomsId: String? = null,
  familyName: String? = null,
  dateOfBirth: LocalDate? = null,
): SearchResultOffender {
  val croOtherNumberResolved = croOtherNumber ?: randomCroNumber()
  return SearchResultOffender(
    id = id ?: randomPpudId(),
    croNumber = croOtherNumberResolved,
    croOtherNumber = croOtherNumberResolved,
    nomsId = nomsId ?: randomNomsId(),
    firstNames = randomString("firstNames"),
    familyName = familyName ?: randomString("familyName"),
    dateOfBirth = dateOfBirth ?: randomDate(),
  )
}

// This will create a request that is useful for mocked testing but uses random values
// so some of the values won't be acceptable to PPUD.
fun generateCreateRecallRequest(
  sentenceDate: LocalDate? = null,
  releaseDate: LocalDate? = null,
  isExtendedSentence: Boolean? = null,
  isInCustody: Boolean? = null,
  riskOfContrabandDetails: String? = null,
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
    riskOfContrabandDetails = riskOfContrabandDetails ?: randomString("riskOfContrabandDetails"),
    riskOfSeriousHarmLevel = riskOfSeriousHarmLevel ?: randomRiskOfSeriousHarmLevel(),
    sentenceDate = sentenceDate ?: randomDate(),
  )
}

fun generateRecall(id: String = randomPpudId()): Recall {
  return Recall(
    id = id,
    allMandatoryDocumentsReceived = "No",
    decisionDateTime = randomTimeToday(),
    isInCustody = Random.nextBoolean(),
    mappaLevel = randomString("mappaLevel"),
    owningTeam = randomString("owningTeam"),
    policeForce = randomString("policeForce"),
    probationArea = randomString("probationArea"),
    recallType = randomString("recallType"),
    receivedDateTime = randomTimeToday(),
    recommendedToDateTime = randomTimeToday(),
    recommendedToOwner = randomString("recommendedToOwner"),
    returnToCustodyNotificationMethod = randomString("returnToCustodyNotificationMethod"),
    revocationIssuedByOwner = randomString("revocationIssuedByOwner"),
  )
}
