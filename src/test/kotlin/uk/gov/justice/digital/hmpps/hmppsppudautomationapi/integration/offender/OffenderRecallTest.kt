package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration.offender

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.PPUD_VALID_USER_TEAM
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.ppudKnownExistingOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomTimeToday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        MandatoryFieldTestData("recommendedTo", createRecallRequestBody(recommendedTo = null)),
        MandatoryFieldTestData(
          "recommendedTo",
          createRecallRequestBody(recommendedTo = "{}"),
          errorFragment = "fullName",
        ),
        MandatoryFieldTestData(
          "recommendedTo",
          createRecallRequestBody(recommendedTo = createPpudUserRequestBody(fullName = "")),
          errorFragment = "fullName",
        ),
        MandatoryFieldTestData(
          "recommendedTo",
          createRecallRequestBody(recommendedTo = createPpudUserRequestBody(teamName = "")),
          errorFragment = "team",
        ),
        MandatoryFieldTestData(
          "riskOfSeriousHarmLevel",
          createRecallRequestBody(riskOfSeriousHarmLevel = ""),
          errorFragment = "RiskOfSeriousHarmLevel",
        ),
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
      recommendedTo: String? = createPpudUserRequestBody(),
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

    private fun createPpudUserRequestBody(
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
  }

  internal class OffenderRecallDataTidyExtension : DataTidyExtensionBase() {
    override fun afterAllTidy() {
      deleteTestOffenders(FAMILY_NAME_PREFIX, testRunId)
    }
  }

  @Test
  fun `given missing token when create recall called then unauthorized is returned`() {
    val uri = constructUri(randomPpudId(), randomPpudId())
    givenMissingTokenWhenCalledThenUnauthorizedReturned(HttpMethod.POST, uri)
  }

  @Test
  fun `given token without recall role when create recall called then forbidden is returned`() {
    val requestBody = createRecallRequestBody()
    val uri = constructUri(randomPpudId(), randomPpudId())
    givenTokenWithoutRecallRoleWhenCalledThenForbiddenReturned(uri, requestBody)
  }

  @Test
  fun `given missing request body when recall called then bad request is returned`() {
    val uri = constructUri(randomPpudId(), randomPpudId())
    webTestClient.post()
      .uri(uri)
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
    val uri = constructUri(randomPpudId(), randomPpudId())
    webTestClient.post()
      .uri(uri)
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
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val releaseId = createTestReleaseInPpud(offenderId, sentenceId)
    val decisionDateTime = randomTimeToday()
    val receivedDateTime = randomTimeToday()
    val requestBody = createRecallRequestBody(
      decisionDateTime = decisionDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      receivedDateTime = receivedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      recommendedTo = createPpudUserRequestBody(PPUD_VALID_USER_FULL_NAME, PPUD_VALID_USER_TEAM),
    )

    val id = postRecall(offenderId, releaseId, requestBody)

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
  fun `given subsequent call with same values in request body when recall called then recall is not created and existing recall Id is returned`() {
    val offenderId = createTestOffenderInPpud()
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val releaseId = createTestReleaseInPpud(offenderId, sentenceId)
    val requestBody = createRecallRequestBody()

    val firstId = postRecall(offenderId, releaseId, requestBody)
    val secondId = postRecall(offenderId, releaseId, requestBody)

    assertEquals(firstId, secondId)
  }

  @Test
  fun `given offender is already in custody when recall called then UAL is unchecked UAL check is not set and return to custody is set`() {
    val offenderId = createTestOffenderInPpud(
      createOffenderRequestBody(
        dateOfSentence = ppudKnownExistingOffender.sentenceDate,
      ),
    )
    val sentenceId = findSentenceIdOnOffender(offenderId)
    val releaseId = createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(isInCustody = "true")

    val id = postRecall(offenderId, releaseId, requestBody)

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
    val releaseId = createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(isInCustody = "false")

    val id = postRecall(offenderId, releaseId, requestBody)

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
    val releaseId = createTestReleaseInPpud(
      offenderId,
      sentenceId,
      releaseRequestBody(dateOfRelease = ppudKnownExistingOffender.releaseDate),
    )
    val requestBody = createRecallRequestBody(riskOfContrabandDetails = randomString("riskOfContrabandDetails"))

    webTestClient.post()
      .uri(constructUri(offenderId, releaseId))
      .headers { it.authToken() }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
      .expectStatus()
      .isCreated
  }

  private fun postRecall(offenderId: String, releaseId: String, requestBody: String): String {
    val idExtractor = ValueConsumer<String>()
    webTestClient.post()
      .uri(constructUri(offenderId, releaseId))
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

  private fun constructUri(offenderId: String, releaseId: String) =
    "/offender/$offenderId/release/$releaseId/recall"
}
