package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration

import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_PROBATION_SERVICE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_FROM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_UNDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "120000")
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    const val FAMILY_NAME_PREFIX = "familyName"

    val testRunId: UUID = UUID.randomUUID()

    fun createOffenderRequestBody(
      address: String? = addressRequestBody(),
      additionalAddresses: String = addressRequestBody(),
      croNumber: String = "",
      custodyType: String = PPUD_VALID_CUSTODY_TYPE,
      dateOfBirth: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      dateOfSentence: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      ethnicity: String = PPUD_VALID_ETHNICITY,
      firstNames: String = randomString("firstNames"),
      familyName: String = "${FAMILY_NAME_PREFIX}-$testRunId",
      gender: String = PPUD_VALID_GENDER,
      indexOffence: String = PPUD_VALID_INDEX_OFFENCE,
      isInCustody: String = Random.nextBoolean().toString(),
      mappaLevel: String = PPUD_VALID_MAPPA_LEVEL,
      nomsId: String = "",
      prisonNumber: String = randomPrisonNumber(),
    ): String {
      return "{" +
        "\"address\":$address, " +
        "\"additionalAddresses\":[$additionalAddresses], " +
        "\"croNumber\":\"$croNumber\", " +
        "\"custodyType\":\"$custodyType\", " +
        "\"dateOfBirth\":\"$dateOfBirth\", " +
        "\"dateOfSentence\":\"$dateOfSentence\", " +
        "\"ethnicity\":\"$ethnicity\", " +
        "\"familyName\":\"$familyName\", " +
        "\"firstNames\":\"$firstNames\", " +
        "\"gender\":\"$gender\", " +
        "\"indexOffence\":\"$indexOffence\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"mappaLevel\":\"$mappaLevel\", " +
        "\"nomsId\":\"$nomsId\", " +
        "\"prisonNumber\":\"$prisonNumber\" " +
        "}"
    }

    fun updateOffenderRequestBody(
      address: String = addressRequestBody(),
      additionalAddresses: String = addressRequestBody(),
      croNumber: String = "",
      dateOfBirth: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      ethnicity: String = PPUD_VALID_ETHNICITY,
      familyName: String = "${FAMILY_NAME_PREFIX}-$testRunId",
      firstNames: String = randomString("firstNames"),
      gender: String = PPUD_VALID_GENDER,
      isInCustody: String = "false",
      nomsId: String = "",
      prisonNumber: String = randomPrisonNumber(),
    ): String {
      return "{" +
        "\"address\":$address, " +
        "\"additionalAddresses\":[$additionalAddresses], " +
        "\"croNumber\":\"$croNumber\", " +
        "\"dateOfBirth\":\"$dateOfBirth\", " +
        "\"ethnicity\":\"$ethnicity\", " +
        "\"familyName\":\"$familyName\", " +
        "\"firstNames\":\"$firstNames\", " +
        "\"gender\":\"$gender\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"nomsId\":\"$nomsId\", " +
        "\"prisonNumber\":\"$prisonNumber\" " +
        "}"
    }

    fun createOrUpdateSentenceRequestBody(
      custodyType: String = PPUD_VALID_CUSTODY_TYPE,
      dateOfSentence: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      espCustodialPeriodYears: Int = Random.nextInt(0, 20),
      espCustodialPeriodMonths: Int = Random.nextInt(0, 20),
      espExtendedPeriodYears: Int = Random.nextInt(0, 20),
      espExtendedPeriodMonths: Int = Random.nextInt(0, 20),
      licenceExpiryDate: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      mappaLevel: String = PPUD_VALID_MAPPA_LEVEL,
      releaseDate: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      sentenceExpiryDate: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      sentenceLengthPartYears: Int = Random.nextInt(0, 20),
      sentenceLengthPartMonths: Int = Random.nextInt(0, 20),
      sentenceLengthPartDays: Int = Random.nextInt(0, 20),
      sentencingCourt: String = randomString("sentCourt"),
    ): String {
      return "{" +
        "\"custodyType\":\"$custodyType\", " +
        "\"dateOfSentence\":\"$dateOfSentence\", " +
        "\"espCustodialPeriod\":{" +
        "  \"years\":\"$espCustodialPeriodYears\", " +
        "  \"months\":\"$espCustodialPeriodMonths\" " +
        "}," +
        "\"espExtendedPeriod\":{" +
        "  \"years\":\"$espExtendedPeriodYears\", " +
        "  \"months\":\"$espExtendedPeriodMonths\" " +
        "}," +
        "\"licenceExpiryDate\":\"$licenceExpiryDate\", " +
        "\"mappaLevel\":\"$mappaLevel\", " +
        "\"releaseDate\":\"$releaseDate\", " +
        "\"sentenceExpiryDate\":\"$sentenceExpiryDate\", " +
        "\"sentenceLength\":{" +
        "  \"partYears\":\"$sentenceLengthPartYears\", " +
        "  \"partMonths\":\"$sentenceLengthPartMonths\", " +
        "  \"partDays\":\"$sentenceLengthPartDays\" " +
        "}," +
        "\"sentencingCourt\":\"$sentencingCourt\" " +
        "}"
    }

    fun releaseRequestBody(
      dateOfRelease: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      postRelease: String = postReleaseRequestBody(),
      releasedFrom: String = PPUD_VALID_RELEASED_FROM,
      releasedUnder: String = PPUD_VALID_RELEASED_UNDER,
    ): String {
      return "{" +
        "\"dateOfRelease\":\"$dateOfRelease\", " +
        "\"postRelease\":$postRelease, " +
        "\"releasedFrom\":\"$releasedFrom\", " +
        "\"releasedUnder\":\"$releasedUnder\" " +
        "}"
    }

    fun postReleaseRequestBody(
      assistantChiefOfficerName: String = randomString("acoName"),
      assistantChiefOfficerFaxEmail: String = randomString("acoFaxEmail"),
      offenderManagerName: String = randomString("omName"),
      offenderManagerFaxEmail: String = randomString("omFaxEmail"),
      offenderManagerTelephone: String = randomPhoneNumber(),
      probationService: String = PPUD_VALID_PROBATION_SERVICE,
      spocName: String = randomString("spocName"),
      spocFaxEmail: String = randomString("spocFaxEmail"),
    ): String {
      return "{" +
        "\"assistantChiefOfficer\":{" +
        "  \"name\":\"$assistantChiefOfficerName\", " +
        "  \"faxEmail\":\"$assistantChiefOfficerFaxEmail\" " +
        "}," +
        "\"offenderManager\":{" +
        "  \"name\":\"$offenderManagerName\", " +
        "  \"faxEmail\":\"$offenderManagerFaxEmail\", " +
        "  \"telephone\":\"$offenderManagerTelephone\" " +
        "}," +
        "\"probationService\":\"$probationService\", " +
        "\"spoc\":{" +
        "  \"name\":\"$spocName\", " +
        "  \"faxEmail\":\"$spocFaxEmail\" " +
        "}" +
        "}"
    }

    fun addressRequestBody(
      premises: String = randomString("premises"),
      line1: String = randomString("line1"),
      line2: String = randomString("line2"),
      postcode: String = randomString("postcode"),
      phoneNumber: String = randomString("phoneNumber"),
    ): String {
      return "{" +
        "\"premises\":\"$premises\", " +
        "\"line1\":\"$line1\", " +
        "\"line2\":\"$line2\", " +
        "\"postcode\":\"$postcode\", " +
        "\"phoneNumber\":\"$phoneNumber\" " +
        "}"
    }
  }

  protected fun HttpHeaders.authToken(
    roles: List<String> = listOf("ROLE_PPUD_AUTOMATION__RECALL__READWRITE"),
    subject: String? = "SOME_USER",
  ) {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "$subject",
        roles = roles,
      ),
    )
  }

  protected fun createTestOffenderInPpud(requestBody: String = createOffenderRequestBody()): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri("/offender")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectBody()
      .jsonPath("offender.id").value(idExtractor)
    val id = idExtractor.value
    Assertions.assertNotNull(id, "ID returned from create offender request is null")
    return id!!
  }

  protected fun findSentenceIdOnOffender(offenderId: String): String {
    val idExtractor = ValueConsumer<String>()
    retrieveOffender(offenderId)
      .jsonPath("offender.sentences[0].id").isNotEmpty
      .jsonPath("offender.sentences[0].id").value(idExtractor)
    val sentenceId = idExtractor.value!!
    return sentenceId
  }

  protected fun createTestReleaseInPpud(offenderId: String, sentenceId: String, requestBody: String = releaseRequestBody()): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri("/offender/$offenderId/sentence/$sentenceId/release")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectBody()
      .jsonPath("release.id").value(idExtractor)
    val id = idExtractor.value
    Assertions.assertNotNull(id, "ID returned from create release request is null")
    return id!!
  }

  protected fun putOffender(offenderId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/offender/$offenderId")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  protected fun retrieveOffender(id: String, includeEmptyReleases: Boolean = false): WebTestClient.BodyContentSpec {
    val includeEmptyReleasesParam = if (includeEmptyReleases) "?includeEmptyReleases=true" else ""
    return webTestClient.get()
      .uri("/offender/$id$includeEmptyReleasesParam")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }

  protected fun retrieveOffenderWhenNotOk(id: String): WebTestClient.ResponseSpec {
    return webTestClient.get()
      .uri("/offender/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
  }

  protected fun givenMissingTokenWhenCalledThenUnauthorizedReturned(method: HttpMethod, uri: String) {
    webTestClient.method(method)
      .uri(uri)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  protected fun givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(uri: String) {
    webTestClient.get()
      .uri(uri)
      .headers { it.authToken(roles = listOf("ANOTHER_ROLE")) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  protected fun givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(
    uri: String,
    requestBody: String,
    method: HttpMethod = HttpMethod.POST,
  ) {
    webTestClient.method(method)
      .uri(uri)
      .headers { it.authToken(roles = listOf("ANOTHER_ROLE")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isForbidden
  }
}
