package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration

import com.google.gson.Gson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_CUSTODY_TYPE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_ETHNICITY
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_GENDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_POLICE_FORCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_PROBATION_SERVICE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_FROM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_RELEASED_UNDER
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_USER_FULL_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_USER_TEAM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDateTime
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "120000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  lateinit var oauthMock: ClientAndServer

  lateinit var documentManagementMock: ClientAndServer

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
      sentencedUnder: String = randomString(),
    ): String {
      return """ 
        {
          "custodyType":"$custodyType",
          "dateOfSentence":"$dateOfSentence",
          "espCustodialPeriod": {
            "years":"$espCustodialPeriodYears",
            "months":"$espCustodialPeriodMonths"
          },
          "espExtendedPeriod": {
            "years":"$espExtendedPeriodYears",
            "months":"$espExtendedPeriodMonths"
          },
          "licenceExpiryDate":"$licenceExpiryDate",
          "mappaLevel":"$mappaLevel",
          "releaseDate":"$releaseDate",
          "sentenceExpiryDate":"$sentenceExpiryDate",
          "sentenceLength": {
            "partYears":"$sentenceLengthPartYears",
            "partMonths":"$sentenceLengthPartMonths",
            "partDays":"$sentenceLengthPartDays"
          },
          "sentencingCourt":"$sentencingCourt",
          "sentencedUnder":"$sentencedUnder"
        }
        """.trimIndent()
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

    fun createRecallRequestBody(
      decisionDateTime: String = randomDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      isInCustody: String = "false",
      isExtendedSentence: String = "false",
      mappaLevel: String = PPUD_VALID_MAPPA_LEVEL,
      policeForce: String = PPUD_VALID_POLICE_FORCE,
      probationArea: String = PPUD_VALID_PROBATION_SERVICE,
      receivedDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      recommendedTo: String? = ppudUserRequestBody(),
      riskOfContrabandDetails: String = "",
      riskOfSeriousHarmLevel: String = RiskOfSeriousHarmLevel.VeryHigh.name,
    ): String {
      return """
        {
          "decisionDateTime":"$decisionDateTime",
          "isInCustody":"$isInCustody",
          "isExtendedSentence":"$isExtendedSentence",
          "mappaLevel":"$mappaLevel",
          "policeForce":"$policeForce",
          "probationArea":"$probationArea",
          "receivedDateTime":"$receivedDateTime",
          "recommendedTo":${recommendedTo ?: "null"},
          "riskOfContrabandDetails":"$riskOfContrabandDetails",
          "riskOfSeriousHarmLevel":"$riskOfSeriousHarmLevel"
        }
      """.trimIndent()
    }

    fun ppudUserRequestBody(
      fullName: String = PPUD_VALID_USER_FULL_NAME,
      teamName: String = PPUD_VALID_USER_TEAM,
    ): String {
      return """
        {
          "fullName":"$fullName",
          "teamName":"$teamName"
        }
      """.trimIndent()
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

  @BeforeAll
  fun beforeAll(@Value("\${documents.storageDirectory}") documentsStorageDirectory: String) {
    File(documentsStorageDirectory).mkdir()
  }

  fun startupMockServers() {
    oauthMock = ClientAndServer.startClientAndServer(9090)
    documentManagementMock = ClientAndServer.startClientAndServer(8442)

    // Mock servers seem to modify the java.util.logging level so set this back
    // to SEVERE so that we don't get Selenium logging superfluous warnings
    Logger.getLogger("").level = Level.SEVERE
  }

  fun resetMockServers() {
    oauthMock.reset()
    documentManagementMock.reset()
    setupOauth()
    setupHealthChecks()
  }

  fun tearDownMockServers() {
    oauthMock.stop()
    documentManagementMock.stop()
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

  protected fun createTestReleaseInPpud(
    offenderId: String,
    sentenceId: String,
    requestBody: String = releaseRequestBody(),
  ): String {
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

  protected fun createTestRecallInPpud(
    offenderId: String,
    releaseId: String,
    requestBody: String = createRecallRequestBody(),
  ): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri("/offender/$offenderId/release/$releaseId/recall")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectBody()
      .jsonPath("recall.id").value(idExtractor)
    val id = idExtractor.value
    Assertions.assertNotNull(id, "ID returned from create recall request is null")
    return id!!
  }

  protected fun deleteTestOffenders(familyNamePrefix: String, testRunId: UUID) {
    webTestClient
      .delete()
      .uri(
        "/offender?" +
          "familyNamePrefix=$familyNamePrefix" +
          "&testRunId=$testRunId",
      )
      .headers { it.dataTidyAuthToken() }
      .exchange()
      .expectStatus()
      .isOk
  }

  protected fun putOffender(offenderId: String, requestBody: String): WebTestClient.ResponseSpec =
    webTestClient.put()
      .uri("/offender/$offenderId")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()

  protected fun retrieveOffender(id: String): WebTestClient.BodyContentSpec {
    return webTestClient.get()
      .uri("/offender/$id")
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

  protected fun retrieveRecall(id: String): WebTestClient.BodyContentSpec {
    return webTestClient.get()
      .uri("/recall/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }

  protected fun givenMissingTokenWhenCalledThenUnauthorizedReturned(method: HttpMethod, uri: String) {
    webTestClient.method(method)
      .uri(uri)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  protected fun setupDocumentManagementMockToReturnDocument(documentId: UUID) {
    val request =
      HttpRequest.request()
        .withPath("/documents/$documentId/file")
        .withHeader("Service-Name", "Making a recall decision Manage a Recall (PPCS) Consider a Recall (CaR)")
    documentManagementMock.`when`(request).respond(
      HttpResponse.response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-file.pdf\"")
        .withBody(ClassPathResource("test-file.pdf").file.readBytes()),
    )
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

  private fun HttpHeaders.dataTidyAuthToken() {
    authToken(listOf("ROLE_PPUD_AUTOMATION__TESTS__READWRITE"), "SOME_USER")
  }

  private fun setupOauth() {
    oauthMock
      .`when`(HttpRequest.request().withPath("/auth/oauth/token"))
      .respond(
        HttpResponse.response()
          .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
          .withBody(Gson().toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer"))),
      )
  }

  private fun setupHealthChecks() {
    oauthMock
      .`when`(HttpRequest.request().withPath("/auth/health/ping"))
      .respond(
        HttpResponse.response()
          .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
          .withBody(Gson().toJson(mapOf("status" to "OK"))),
      )

    documentManagementMock
      .`when`(HttpRequest.request().withPath("/health/ping"))
      .respond(
        HttpResponse.response()
          .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
          .withBody(Gson().toJson(mapOf("status" to "OK"))),
      )
  }
}
