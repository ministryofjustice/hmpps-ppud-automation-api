package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.DocumentCategory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.OffenderAddress
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SearchResultOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.Recall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.AddMinuteRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UserSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.random.Random

const val PPUD_VALID_CUSTODY_TYPE = "Determinate"

const val PPUD_VALID_ETHNICITY = "Chinese"

const val PPUD_VALID_ETHNICITY_2 = "Other Ethnic Group"

const val PPUD_VALID_GENDER = "M"

const val PPUD_VALID_GENDER_2 = "F ( Was M )"

const val PPUD_VALID_INDEX_OFFENCE = "ADMINISTER DRUGS"

const val PPUD_VALID_INDEX_OFFENCE_2 = "CRIMINAL DAMAGE"

// Watch out for the different hyphens in the dropdown options
const val PPUD_VALID_MAPPA_LEVEL = "Level 1 – Single Agency Management"

// Watch out for the different hyphens in the dropdown options
const val PPUD_VALID_MAPPA_LEVEL_2 = "Level 2 – Local Inter-Agency Management"

const val PPUD_VALID_POLICE_FORCE = "Kent Police"

const val PPUD_VALID_PROBATION_SERVICE = "Merseyside"

const val PPUD_VALID_RELEASED_FROM = "HMP Wakefield"

const val PPUD_VALID_RELEASED_FROM_2 = "HMP Shepton Mallet"

const val PPUD_VALID_RELEASED_UNDER = "CJA 1991"

const val PPUD_VALID_RELEASED_UNDER_2 = "CJA 2008"

const val PPUD_VALID_USER_FULL_NAME = "Consider a Recall Test Admin"

const val PPUD_VALID_USER_TEAM = "Performance Management"

const val PPUD_IMMIGRATION_STATUS = "Not Applicable"

const val PPUD_LICENCE_TYPE = "Standard"

const val PPUD_PRISONER_CATEGORY = "Not Applicable"

const val PPUD_STATUS = "Recalled [*]"

const val PPUD_YOUNG_OFFENDER_YES = "Yes - Named"

const val PPUD_YOUNG_OFFENDER_NO = "No"

/**
 * This is an offender that exists in PPUD InternalTest with values that are known and
 * used in the tests.
 */
internal val ppudKnownExistingOffender: TestOffender
  get() = TestOffender(
    id = "4F6666656E64657269643D313632393134G721H665",
    dateOfBirth = "1969-03-02",
    familyName = "Mitchell",
    firstNames = "Mandy Car Test",
    prisonNumber = "XX4321",
    releaseDate = "2013-02-02",
    sentenceDate = "2003-06-12",
  )

internal const val PPUD_OFFENDER_ID_WITH_EMPTY_RELEASE = "4F6666656E64657249643D313732323738G687H671"

internal const val PPUD_OFFENDER_ID_WITH_PAGED_ADDRESSES = "4F6666656E64657249643D313737373331G685H672"

private const val SIXTY_YEARS_IN_DAYS: Long = 21900

private const val SECONDS_IN_A_DAY: Long = 86400

fun randomString(prefix: String = "random"): String = "$prefix-${UUID.randomUUID()}"

fun randomCroNumber(): String {
  val serial = Random.nextInt(100000, 999999)
  val year = Random.nextInt(10, 23)
  return "$serial/${year}A"
}

fun randomNomsId(): String {
  val serial = Random.nextInt(1000, 9999)
  return "A${serial}BC"
}

private val prisonNumbers = 1000.rangeTo(9999).map { "LW$it" }.toMutableSet()

fun randomPrisonNumber(): String {
  val number = prisonNumbers.random()
  prisonNumbers.remove(number)
  return number
}

fun randomDate(): LocalDate = LocalDate.parse("2005-01-01").minusDays(Random.nextLong(SIXTY_YEARS_IN_DAYS))

fun randomDateTime(): LocalDateTime {
  val earliest = LocalDateTime.parse("2000-01-01T00:00:00")
  val latest = LocalDateTime.now()
  val randomEpochSecond = Random.nextLong(earliest.toEpochSecond(ZoneOffset.UTC), latest.toEpochSecond(ZoneOffset.UTC))
  return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, ZoneOffset.UTC)
}

fun randomTimeToday(): LocalDateTime = LocalDate.now().atTime(LocalTime.ofSecondOfDay(Random.nextLong(SECONDS_IN_A_DAY)))

fun randomPhoneNumber(): String {
  val number = Random.nextInt(100000000, 999999999)
  return "0$number"
}

fun randomPostcode(): String {
  val postcodes = listOf(
    "XX73 9XX",
    "XX739XX",
    "AC12 3ZZ",
    "ZZ1 1FF",
    "AA1W 1FF",
  )
  return postcodes.random()
}

/**
 * Generate a randomPpudId. Note that this is not a properly valid ID (i.e. checksum isn't
 * correct), but it looks like a PPUD ID.
 */
fun randomPpudId(): String {
  val randomSerial = Random.nextInt(1000000, 9999999)
  return "4F${randomSerial}E64657269643D313632393134G721H665"
}

fun randomLookupName(exclude: List<LookupName> = listOf()): LookupName = LookupName.entries.filter { exclude.contains(it).not() }.shuffled().first()

fun randomRiskOfSeriousHarmLevel(): RiskOfSeriousHarmLevel = RiskOfSeriousHarmLevel.entries.shuffled().first()

fun randomDocumentCategory(exclude: DocumentCategory? = null): DocumentCategory = DocumentCategory.entries.shuffled().first { it != exclude }

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateCreateOffenderRequest(): CreateOffenderRequest = CreateOffenderRequest(
  address = generateOffenderAddress(),
  croNumber = randomCroNumber(),
  custodyType = randomString("custodyType"),
  dateOfBirth = randomDate(),
  dateOfSentence = randomDate(),
  ethnicity = randomString("ethnicity"),
  firstNames = randomString("firstNames"),
  familyName = randomString("familyName"),
  gender = randomString("gender"),
  indexOffence = randomString("indexOffence"),
  isInCustody = Random.nextBoolean(),
  mappaLevel = randomString("mappaLevel"),
  nomsId = randomNomsId(),
  prisonNumber = randomPrisonNumber(),
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateUpdateOffenderRequest(): UpdateOffenderRequest = UpdateOffenderRequest(
  croNumber = randomCroNumber(),
  dateOfBirth = randomDate(),
  ethnicity = randomString("ethnicity"),
  familyName = randomString("familyName"),
  firstNames = randomString("firstNames"),
  gender = randomString("gender"),
  isInCustody = Random.nextBoolean(),
  prisonNumber = randomPrisonNumber(),
)

fun generateOffender(id: String = randomPpudId()): Offender {
  val croOtherNumber = randomCroNumber()
  return Offender(
    id = id,
    address = generateOffenderAddress(),
    caseworker = randomString("caseworker"),
    comments = randomString("comments"),
    croOtherNumber = croOtherNumber,
    dateOfBirth = randomDate(),
    ethnicity = randomString("ethnicity"),
    familyName = randomString("familyName"),
    firstNames = randomString("firstNames"),
    gender = randomString("gender"),
    immigrationStatus = randomString("immigrationStatus"),
    isInCustody = Random.nextBoolean(),
    nomsId = randomNomsId(),
    prisonerCategory = randomString("prisonerCategory"),
    prisonNumber = randomPrisonNumber(),
    status = randomString("status"),
    youngOffender = randomString("youngOffender"),
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

fun generateOffenderAddress(): OffenderAddress = OffenderAddress(
  premises = randomString("premises"),
  line1 = randomString("line1"),
  line2 = randomString("line2"),
  postcode = randomString("postcode"),
  phoneNumber = randomString("phoneNumber"),
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateCreateOrUpdateSentenceRequest(
  custodyType: String = randomString("custodyType"),
  dateOfSentence: LocalDate = randomDate(),
  mappaLevel: String = randomString("mappaLevel"),
  sentencingCourt: String = randomString("sentencingCourt"),
): CreateOrUpdateSentenceRequest = CreateOrUpdateSentenceRequest(
  custodyType = custodyType,
  dateOfSentence = dateOfSentence,
  espCustodialPeriod = null,
  espExtendedPeriod = null,
  licenceExpiryDate = null,
  mappaLevel = mappaLevel,
  releaseDate = randomDate(),
  sentenceExpiryDate = null,
  sentenceLength = null,
  sentencingCourt = sentencingCourt,
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateUpdateOffenceRequest(): UpdateOffenceRequest = UpdateOffenceRequest(
  indexOffence = randomString("indexOffence"),
  dateOfIndexOffence = randomDate(),
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateCreateOrUpdateReleaseRequest(
  dateOfRelease: LocalDate = randomDate(),
  releasedFrom: String = randomString("releasedFrom"),
  releasedUnder: String = randomString("releasedUnder"),
): CreateOrUpdateReleaseRequest = CreateOrUpdateReleaseRequest(
  dateOfRelease = dateOfRelease,
  releasedFrom = releasedFrom,
  releasedUnder = releasedUnder,
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateCreateRecallRequest(
  isExtendedSentence: Boolean? = null,
  isInCustody: Boolean? = null,
  receivedDateTime: LocalDateTime = randomTimeToday(),
  recommendedTo: PpudUser = generatePpudUser(),
  riskOfContrabandDetails: String? = null,
  riskOfSeriousHarmLevel: RiskOfSeriousHarmLevel? = null,
): CreateRecallRequest = CreateRecallRequest(
  decisionDateTime = randomTimeToday(),
  isExtendedSentence = isExtendedSentence ?: Random.nextBoolean(),
  isInCustody = isInCustody ?: Random.nextBoolean(),
  mappaLevel = randomString("mappaLevel"),
  policeForce = randomString("policeForce"),
  probationArea = randomString("probationArea"),
  receivedDateTime = receivedDateTime,
  recommendedTo = recommendedTo,
  riskOfContrabandDetails = riskOfContrabandDetails ?: randomString("riskOfContrabandDetails"),
  riskOfSeriousHarmLevel = riskOfSeriousHarmLevel ?: randomRiskOfSeriousHarmLevel(),
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateUploadMandatoryDocumentRequest(
  documentId: UUID = UUID.randomUUID(),
  owningCaseworker: PpudUser = generatePpudUser(),
): UploadMandatoryDocumentRequest = UploadMandatoryDocumentRequest(
  documentId = documentId,
  category = randomDocumentCategory(),
  owningCaseworker = owningCaseworker,
)

/**
 * This will create a request that is useful for mocked testing but uses random values
 * so some of the values won't be acceptable to PPUD.
 */
fun generateUploadAdditionalDocumentRequest(
  documentId: UUID = UUID.randomUUID(),
  owningCaseworker: PpudUser = generatePpudUser(),
): UploadAdditionalDocumentRequest = UploadAdditionalDocumentRequest(
  documentId = documentId,
  title = randomString("title"),
  owningCaseworker = owningCaseworker,
)

fun generateAddMinuteRequest(
  subject: String = randomString("subject"),
  text: String = randomString("text"),
): AddMinuteRequest = AddMinuteRequest(
  subject = subject,
  text = text,
)

fun generateRecall(id: String = randomPpudId()): Recall = Recall(
  id = id,
  allMandatoryDocumentsReceived = "No",
  decisionDateTime = randomTimeToday(),
  documents = emptyList(),
  isInCustody = Random.nextBoolean(),
  minutes = emptyList(),
  missingMandatoryDocuments = emptyList(),
  owningTeam = randomString("owningTeam"),
  policeForce = randomString("policeForce"),
  probationArea = randomString("probationArea"),
  recallType = randomString("recallType"),
  receivedDateTime = randomTimeToday(),
  mappaLevel = randomString("mappaLevel"),
  recommendedToOwner = randomString("recommendedToOwner"),
  recommendedToDateTime = randomTimeToday(),
  revocationIssuedByOwner = randomString("revocationIssuedByOwner"),
  returnToCustodyNotificationMethod = randomString("returnToCustodyNotificationMethod"),
)

fun generatePpudUser(): PpudUser = PpudUser(
  fullName = randomString("fullName"),
  teamName = randomString("teamName"),
)

fun generateUserSearchRequest(fullName: String?, userName: String?): UserSearchRequest = UserSearchRequest(fullName, userName)
