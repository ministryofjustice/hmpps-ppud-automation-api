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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomCroNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomNomsId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPncNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.format.DateTimeFormatter
import java.util.UUID

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
      croNumber: String = randomCroNumber(),
      custodyType: String = PPUD_VALID_CUSTODY_TYPE,
      dateOfBirth: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      dateOfSentence: String = randomDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
      ethnicity: String = PPUD_VALID_ETHNICITY,
      firstNames: String = randomString("firstNames"),
      familyName: String = "${FAMILY_NAME_PREFIX}-$testRunId",
      gender: String = PPUD_VALID_GENDER,
      indexOffence: String = PPUD_VALID_INDEX_OFFENCE,
      mappaLevel: String = PPUD_VALID_MAPPA_LEVEL,
      nomsId: String = randomNomsId(),
      pncNumber: String = randomPncNumber(),
      prisonNumber: String = randomPrisonNumber(),
    ): String {
      return "{" +
        "\"croNumber\":\"${croNumber}\", " +
        "\"custodyType\":\"${custodyType}\", " +
        "\"dateOfBirth\":\"$dateOfBirth\", " +
        "\"dateOfSentence\":\"$dateOfSentence\", " +
        "\"ethnicity\":\"$ethnicity\", " +
        "\"familyName\":\"$familyName\", " +
        "\"firstNames\":\"$firstNames\", " +
        "\"gender\":\"$gender\", " +
        "\"indexOffence\":\"$indexOffence\", " +
        "\"mappaLevel\":\"$mappaLevel\", " +
        "\"nomsId\":\"$nomsId\", " +
        "\"pncNumber\":\"$pncNumber\", " +
        "\"prisonNumber\":\"$prisonNumber\" " +
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

  protected fun retrieveOffender(id: String): WebTestClient.BodyContentSpec {
    return webTestClient.get()
      .uri("/offender/$id")
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
