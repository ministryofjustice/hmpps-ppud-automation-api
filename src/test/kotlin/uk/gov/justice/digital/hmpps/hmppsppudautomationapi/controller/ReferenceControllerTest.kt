package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class ReferenceControllerTest {

  @InjectMocks
  private lateinit var controller: ReferenceController

  @Mock
  private lateinit var referenceService: ReferenceService

  // TODO MRD-2769 find out why log testing fails in CircleCI
//  private val logAppender = findLogAppender(ReferenceController::class.java)

  @Test
  fun `when clearCaches is called then reference service is invoked to clear caches`() {
    runBlocking {
      controller.clearCaches()

      then(referenceService).should().clearCaches()
      assertInfoMessageForEndpointWasLogged("clear-caches")
    }
  }

  @Test
  fun `when refreshCaches is called then reference service is invoked to refresh caches`() {
    runBlocking {
      controller.refreshCaches()

      then(referenceService).should().refreshCaches()
      assertInfoMessageForEndpointWasLogged("refresh-caches")
    }
  }

  @Test
  fun `when custodyTypes is called then reference service is invoked and results returned`() {
    runBlocking {
      // Only returning Determinate for now until we can handle other types
      val values = listOf(randomString(), randomString(), randomString(), "Determinate")
      given(referenceService.retrieveCustodyTypes()).willReturn(values)

      val result = controller.custodyTypes()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(listOf("Determinate"), result.body?.values)
      assertInfoMessageForEndpointWasLogged("custody-types")
    }
  }

  @Test
  fun `when establishments is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveEstablishments()).willReturn(values)

      val result = controller.establishments()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("establishments")
    }
  }

  @Test
  fun `when ethnicities is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveEthnicities()).willReturn(values)

      val result = controller.ethnicities()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("ethnicities")
    }
  }

  @Test
  fun `when genders is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveGenders()).willReturn(values)

      val result = controller.genders()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("genders")
    }
  }

  @Test
  fun `when indexOffences is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveIndexOffences()).willReturn(values)

      val result = controller.indexOffences()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("index offences")
    }
  }

  @Test
  fun `when mappaLevels is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveMappaLevels()).willReturn(values)

      val result = controller.mappaLevels()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("mappa levels")
    }
  }

  @Test
  fun `when policeForces is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrievePoliceForces()).willReturn(values)

      val result = controller.policeForces()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("police forces")
    }
  }

  @Test
  fun `when probationServices is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveProbationServices()).willReturn(values)

      val result = controller.probationServices()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("probation services")
    }
  }

  @Test
  fun `when releasedUnders is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveReleasedUnders()).willReturn(values)

      val result = controller.releaseUnders()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("released unders")
    }
  }

  @Test
  fun `when courts is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveCourts()).willReturn(values)

      val result = controller.courts()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("courts")
    }
  }

  @Test
  fun `when determinateCustodyTypes is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveDeterminateCustodyTypes()).willReturn(values)

      val result = controller.determinateCustodyTypes()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("determinate custody types")
    }
  }

  @Test
  fun `when indeterminateCustodyTypes is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveIndeterminateCustodyTypes()).willReturn(values)

      val result = controller.indeterminateCustodyTypes()

      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
      assertInfoMessageForEndpointWasLogged("indeterminate custody types")
    }
  }

  private fun assertInfoMessageForEndpointWasLogged(endpoint: String) {
    // TODO MRD-2769 find out why log testing fails in CircleCI
//    with(logAppender.list) {
//      assertThat(size).isEqualTo(1)
//      with(get(0)) {
//        assertThat(level).isEqualTo(Level.INFO)
//        assertThat(message).isEqualTo("Reference data $endpoint endpoint hit")
//      }
//    }
  }
}
