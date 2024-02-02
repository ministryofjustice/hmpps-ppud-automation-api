package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.RiskOfSeriousHarmLevel
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.IsSameDayAs
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.ValueConsumer
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.isNull
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.withoutSeconds
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.DataTidyExtensionBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.MandatoryFieldTestData
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_POLICE_FORCE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_PROBATION_SERVICE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_USER_FULL_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_USER_FULL_NAME_AND_TEAM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.function.Consumer
import java.util.stream.Stream

@ExtendWith(OffenderRecallTest.OffenderRecallDataTidyExtension::class)
class OffenderRecallTest : IntegrationTestBase() {

  companion object {

    private const val PPUD_EXPECTED_RECALL_TYPE = "Standard"

    private const val PPUD_EXPECTED_OWNING_TEAM = "Recall 1"

    private const val PPUD_EXPECTED_REVOCATION_ISSUED_BY_OWNER = "EO Officer"

    private const val PPUD_EXPECTED_RETURN_TO_CUSTODY_NOTIFICATION_METHOD = "Not Applicable"

    @JvmStatic
    private fun mandatoryFieldTestData(): Stream<MandatoryFieldTestData> {
      return Stream.of(
        MandatoryFieldTestData("decisionDateTime", createRecallRequestBody(decisionDateTime = "")),
        MandatoryFieldTestData("mappaLevel", createRecallRequestBody(mappaLevel = "")),
        MandatoryFieldTestData("policeForce", createRecallRequestBody(policeForce = "")),
        MandatoryFieldTestData("probationArea", createRecallRequestBody(probationArea = "")),
        MandatoryFieldTestData("receivedDateTime", createRecallRequestBody(receivedDateTime = "")),
        MandatoryFieldTestData("recommendedToOwner", createRecallRequestBody(recommendedToOwner = "")),
        MandatoryFieldTestData("releaseDate", createRecallRequestBody(releaseDate = "")),
        MandatoryFieldTestData(
          "riskOfSeriousHarmLevel",
          createRecallRequestBody(riskOfSeriousHarmLevel = ""),
          errorFragment = "RiskOfSeriousHarmLevel",
        ),
        MandatoryFieldTestData("sentenceDate", createRecallRequestBody(sentenceDate = "")),
      )
    }

    private fun createRecallRequestBody(
      decisionDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      isInCustody: String = "false",
      isExtendedSentence: String = "false",
      mappaLevel: String = PPUD_VALID_MAPPA_LEVEL,
      policeForce: String = PPUD_VALID_POLICE_FORCE,
      probationArea: String = PPUD_VALID_PROBATION_SERVICE,
      receivedDateTime: String = randomTimeToday().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      recommendedToOwner: String = PPUD_VALID_USER_FULL_NAME_AND_TEAM,
      // TODO: Replace with Sentence ID
      releaseDate: String = ppudKnownExistingOffender.releaseDate,
      riskOfContrabandDetails: String = "",
      riskOfSeriousHarmLevel: String = RiskOfSeriousHarmLevel.VeryHigh.name,
      // TODO: Replace with Sentence ID
      sentenceDate: String = ppudKnownExistingOffender.sentenceDate,
    ): String {
      return "{" +
        "\"decisionDateTime\":\"${decisionDateTime}\", " +
        "\"isInCustody\":\"$isInCustody\", " +
        "\"isExtendedSentence\":\"$isExtendedSentence\", " +
        "\"mappaLevel\":\"${mappaLevel}\", " +
        "\"policeForce\":\"${policeForce}\", " +
        "\"probationArea\":\"$probationArea\", " +
        "\"receivedDateTime\":\"${receivedDateTime}\", " +
        "\"recommendedToOwner\":\"$recommendedToOwner\", " +
        "\"releaseDate\":\"$releaseDate\", " +
        "\"riskOfContrabandDetails\":\"$riskOfContrabandDetails\", " +
        "\"riskOfSeriousHarmLevel\":\"$riskOfSeriousHarmLevel\", " +
        "\"sentenceDate\":\"$sentenceDate\" " +
        "}"
    }
  }

  internal class OffenderRecallDataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing token when create recall called then unauthorized is returned`() {
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, "/offender/${randomPpudId()}/recall")
  }

  @Test
  fun `given token without recall role when create recall called then forbidden is returned`() {
    val requestBody = createRecallRequestBody()
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned("/offender/${randomPpudId()}/recall", requestBody)
  }

  @Test
  fun `given missing request body when recall called then bad request is returned`() {
    webTestClient.post()
      .uri("/offender/${randomPpudId()}/recall")
      .headers { it.authToken() }
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @ParameterizedTest
  @MethodSource("mandatoryFieldTestData")
  fun `given missing mandatory value in request body when recall called then bad request is returned`(
    data: MandatoryFieldTestData,
  ) {
    val errorFragment = data.errorFragment ?: data.propertyName
    webTestClient.post()
      .uri("/offender/${randomPpudId()}/recall")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(data.requestBody))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("userMessage")
      .value(Consumer<String> { assertThat(it).contains(errorFragment) })
  }

  @Test
  fun `given valid values in request body when recall called then recall is created using supplied values and 201 created and recall Id are returned`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfSentence = ppudKnownExistingOffender.sentenceDate,
      ),
    )
    val sentenceId = findSentenceIdOnOffender(offenderId)
    createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val decisionDateTime = randomTimeToday()
    val receivedDateTime = randomTimeToday().truncatedTo(ChronoUnit.MINUTES)
    val requestBody = createRecallRequestBody(
      decisionDateTime = decisionDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      receivedDateTime = receivedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )

    val id = postRecall(offenderId, requestBody)

    val retrieved = retrieveRecall(id)
    retrieved.jsonPath("recall.id").isEqualTo(id)
      .jsonPath("recall.allMandatoryDocumentsReceived").isEqualTo("No")
      .jsonPath("recall.decisionDateTime").isEqualTo(decisionDateTime.withoutSeconds())
      .jsonPath("recall.mappaLevel").isEqualTo(PPUD_VALID_MAPPA_LEVEL)
      .jsonPath("recall.owningTeam").isEqualTo(PPUD_EXPECTED_OWNING_TEAM)
      .jsonPath("recall.policeForce").isEqualTo(PPUD_VALID_POLICE_FORCE)
      .jsonPath("recall.probationArea").isEqualTo(PPUD_VALID_PROBATION_SERVICE)
      .jsonPath("recall.receivedDateTime").isEqualTo(receivedDateTime.withoutSeconds())
      .jsonPath("recall.recommendedToOwner").isEqualTo(PPUD_VALID_USER_FULL_NAME)
      .jsonPath("recall.recallType").isEqualTo(PPUD_EXPECTED_RECALL_TYPE)
      .jsonPath("recall.revocationIssuedByOwner").isEqualTo(PPUD_EXPECTED_REVOCATION_ISSUED_BY_OWNER)
    val recommendedToDateTimeIsToday = IsSameDayAs(LocalDate.now())
    retrieved.jsonPath("recall.recommendedToDateTime").value(recommendedToDateTimeIsToday)
    assertTrue(recommendedToDateTimeIsToday.isSameDay, "recommendedToDateTime is not today")
  }

  @Test
  fun `given offender is already in custody when recall called then UAL is unchecked UAL check is not set and return to custody is set`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfSentence = ppudKnownExistingOffender.sentenceDate,
      ),
    )
    val sentenceId = findSentenceIdOnOffender(offenderId)
    createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(isInCustody = "true")

    val id = postRecall(offenderId, requestBody)

    val retrieved = retrieveRecall(id)
    retrieved
      .jsonPath("recall.isInCustody").isEqualTo("true")
      .jsonPath("recall.nextUalCheck").value(isNull())
      .jsonPath("recall.returnToCustodyNotificationMethod")
      .isEqualTo(PPUD_EXPECTED_RETURN_TO_CUSTODY_NOTIFICATION_METHOD)
  }

  @Test
  fun `given offender is not in custody when recall called then UAL is checked UAL check is set and return to custody is not set`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfSentence = ppudKnownExistingOffender.sentenceDate,
      ),
    )
    val sentenceId = findSentenceIdOnOffender(offenderId)
    createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(isInCustody = "false")

    val id = postRecall(offenderId, requestBody)

    val retrieved = retrieveRecall(id)
    retrieved
      .jsonPath("recall.isInCustody").isEqualTo("false")
      .jsonPath("recall.nextUalCheck")
      .isEqualTo(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE))
      .jsonPath("recall.returnToCustodyNotificationMethod").isEqualTo("Not Specified")
  }

  // TODO: This is a bit of a rubbish test. Can we assert that riskOfContrabandDetails was used?
  @Test
  fun `given risk of contraband details when recall called then 201 created and recall Id are returned`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfSentence = ppudKnownExistingOffender.sentenceDate,
      ),
    )
    val sentenceId = findSentenceIdOnOffender(offenderId)
    createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(riskOfContrabandDetails = randomString("riskOfContrabandDetails"))

    webTestClient.post()
      .uri("/offender/$offenderId/recall")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
  }

  private fun postRecall(offenderId: String, requestBody: String): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri("/offender/$offenderId/recall")
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
      .expectBody()
      .jsonPath("recall.id").value(idExtractor)
    val id = idExtractor.value
    assertNotNull(id, "ID returned from create recall request is null")
    assertTrue(id!!.isNotEmpty(), "ID returned from create recall request is empty")
    return id
  }

  private fun retrieveRecall(id: String): BodyContentSpec {
    return webTestClient.get()
      .uri("/recall/$id")
      .headers { it.authToken() }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }
}
