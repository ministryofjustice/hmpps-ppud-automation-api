package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.ReferenceService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class ReferenceControllerTest {

  @Mock
  lateinit var referenceService: ReferenceService

  private lateinit var controller: ReferenceController

  @BeforeEach
  fun beforeEach() {
    controller = ReferenceController(referenceService)
  }

  @Test
  fun `when clearCaches is called then reference service is invoked to clear caches`() {
    runBlocking {
      controller.clearCaches()

      then(referenceService).should().clearCaches()
    }
  }

  @Test
  fun `when refreshCaches is called then reference service is invoked to refresh caches`() {
    runBlocking {
      controller.refreshCaches()

      then(referenceService).should().refreshCaches()
    }
  }

  @Test
  fun `when custodyTypes is called then reference service is invoked and results returned`() {
    runBlocking {
      // Only returning Determinate for now until we can handle other types
      val values = listOf(randomString(), randomString(), randomString(), "Determinate")
      given(referenceService.retrieveCustodyTypes()).willReturn(values)

      val result = controller.custodyTypes()

      then(referenceService).should().retrieveCustodyTypes()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(listOf("Determinate"), result.body?.values)
    }
  }

  @Test
  fun `when establishments is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveEstablishments()).willReturn(values)

      val result = controller.establishments()

      then(referenceService).should().retrieveEstablishments()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when ethnicities is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveEthnicities()).willReturn(values)

      val result = controller.ethnicities()

      then(referenceService).should().retrieveEthnicities()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when genders is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveGenders()).willReturn(values)

      val result = controller.genders()

      then(referenceService).should().retrieveGenders()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when indexOffences is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveIndexOffences()).willReturn(values)

      val result = controller.indexOffences()

      then(referenceService).should().retrieveIndexOffences()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when mappaLevels is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveMappaLevels()).willReturn(values)

      val result = controller.mappaLevels()

      then(referenceService).should().retrieveMappaLevels()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when policeForces is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrievePoliceForces()).willReturn(values)

      val result = controller.policeForces()

      then(referenceService).should().retrievePoliceForces()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when probationServices is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveProbationServices()).willReturn(values)

      val result = controller.probationServices()

      then(referenceService).should().retrieveProbationServices()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when releasedUnders is called then reference service is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveReleasedUnders()).willReturn(values)

      val result = controller.releaseUnders()

      then(referenceService).should().retrieveReleasedUnders()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }
}
