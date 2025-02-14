package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.recall.CreatedRecall
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateCreateRecallRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomPpudId
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class OffenderControllerTest {

  @Mock
  lateinit var ppudClient: OperationalPpudClient

  @Mock
  lateinit var createdOffender: CreatedOffender

  private lateinit var controller: OffenderController

  @BeforeEach
  fun beforeEach() {
    controller = OffenderController(ppudClient)
  }

  @Test
  fun `given search criteria when search is called then criteria are passed to PPUD client`() {
    runBlocking {
      val croNumber = "A123"
      val nomsId = "B456"
      val familyName = "Smith"
      val dateOfBirth = LocalDate.parse("2000-01-01")
      val criteria = OffenderSearchRequest(
        croNumber,
        nomsId,
        familyName,
        dateOfBirth,
      )
      given(ppudClient.searchForOffender(croNumber, nomsId, familyName, dateOfBirth)).willReturn(emptyList())

      controller.search(criteria)

      then(ppudClient).should().searchForOffender(croNumber, nomsId, familyName, dateOfBirth)
    }
  }

  @Test
  fun `given offender ID when get is called then offender is returned`() {
    runBlocking {
      val id = randomPpudId()
      val offender = generateOffender(id = id)
      given(ppudClient.retrieveOffender(id)).willReturn(offender)

      val result = controller.get(id)

      then(ppudClient).should().retrieveOffender(id)
      assertEquals(offender, result.body?.offender)
    }
  }

  @Test
  fun `given offender data when createOffender is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderRequest = generateCreateOffenderRequest()
      given(ppudClient.createOffender(offenderRequest)).willReturn(createdOffender)

      controller.createOffender(offenderRequest)

      then(ppudClient).should().createOffender(offenderRequest)
    }
  }

  @Test
  fun `given offender creation succeeds when createOffender is called then offender details are returned`() {
    runBlocking {
      val offenderRequest = generateCreateOffenderRequest()
      given(ppudClient.createOffender(offenderRequest)).willReturn(createdOffender)

      val result = controller.createOffender(offenderRequest)

      assertEquals(HttpStatus.CREATED, result.statusCode)
      assertEquals(createdOffender, result.body?.offender)
    }
  }

  @Test
  fun `given offender id and offender data when updateOffender is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val offenderRequest = generateUpdateOffenderRequest()

      controller.updateOffender(offenderId, offenderRequest)

      then(ppudClient).should().updateOffender(offenderId, offenderRequest)
    }
  }

  @Test
  fun `given offender ID and sentence data when createSentence is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      given(ppudClient.createSentence(offenderId, request)).willReturn(CreatedSentence(""))

      controller.createSentence(offenderId, request)

      then(ppudClient).should().createSentence(offenderId, request)
    }
  }

  @Test
  fun `given sentence creation succeeds when createSentence is called then sentence Id is returned`() {
    runBlocking {
      val offenderId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()
      val sentenceId = randomPpudId()
      given(ppudClient.createSentence(offenderId, request)).willReturn(CreatedSentence(sentenceId))

      val result = controller.createSentence(offenderId, request)

      assertEquals(HttpStatus.CREATED, result.statusCode)
      assertEquals(sentenceId, result.body?.sentence?.id)
    }
  }

  @Test
  fun `given offender ID and sentence ID and sentence data when updateSentence is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateSentenceRequest()

      controller.updateSentence(offenderId, sentenceId, request)

      then(ppudClient).should().updateSentence(offenderId, sentenceId, request)
    }
  }

  @Test
  fun `given offence data when updateOffence is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateUpdateOffenceRequest()

      controller.updateOffence(offenderId, sentenceId, request)

      then(ppudClient).should().updateOffence(offenderId, sentenceId, request)
    }
  }

  @Test
  fun `given release data when createOrUpdateRelease is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val request = generateCreateOrUpdateReleaseRequest()
      given(ppudClient.createOrUpdateRelease(offenderId, sentenceId, request)).willReturn(CreatedOrUpdatedRelease(""))

      controller.createOrUpdateRelease(offenderId, sentenceId, request)

      then(ppudClient).should().createOrUpdateRelease(offenderId, sentenceId, request)
    }
  }

  @Test
  fun `given release data when createOrUpdateRelease is called then release ID is returned`() {
    runBlocking {
      val offenderId = randomPpudId()
      val sentenceId = randomPpudId()
      val releaseId = randomPpudId()
      val request = generateCreateOrUpdateReleaseRequest()
      given(ppudClient.createOrUpdateRelease(offenderId, sentenceId, request)).willReturn(
        CreatedOrUpdatedRelease(
          releaseId,
        ),
      )

      val result = controller.createOrUpdateRelease(offenderId, sentenceId, request)

      assertEquals(HttpStatus.OK, result.statusCode)
      assertEquals(releaseId, result.body?.release?.id)
    }
  }

  @Test
  fun `given recall data when createRecall is called then data is passed to PPUD client`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val recallRequest = generateCreateRecallRequest()
      given(ppudClient.createRecall(offenderId, releaseId, recallRequest)).willReturn(CreatedRecall(""))

      controller.createRecall(offenderId, releaseId, recallRequest)

      then(ppudClient).should().createRecall(offenderId, releaseId, recallRequest)
    }
  }

  @Test
  fun `given recall creation succeeds when createRecall is called then recall Id is returned`() {
    runBlocking {
      val offenderId = randomPpudId()
      val releaseId = randomPpudId()
      val recallId = randomPpudId()
      val recallRequest = generateCreateRecallRequest()
      given(
        ppudClient.createRecall(offenderId, releaseId, recallRequest),
      ).willReturn(CreatedRecall(recallId))

      val result = controller.createRecall(offenderId, releaseId, recallRequest)

      assertEquals(HttpStatus.CREATED, result.statusCode)
      assertEquals(recallId, result.body?.recall?.id)
    }
  }

  @Test
  fun `given deletion criteria when deleteTestOffenders is called then family name is passed to PPUD client`() {
    runBlocking {
      val familyNamePrefix = "prefix"
      val testRunDate = UUID.randomUUID()

      controller.deleteTestOffenders(familyNamePrefix, testRunDate)
      val expected = "$familyNamePrefix-$testRunDate"

      then(ppudClient).should().deleteOffenders(expected)
    }
  }
}
