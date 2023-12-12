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
  fun `when custodyTypes is called then ppud client is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveCustodyTypes()).willReturn(values)

      val result = controller.custodyTypes()

      then(referenceService).should().retrieveCustodyTypes()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }

  @Test
  fun `when establishments is called then ppud client is invoked and results returned`() {
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
  fun `when ethnicities is called then ppud client is invoked and results returned`() {
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
  fun `when genders is called then ppud client is invoked and results returned`() {
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
  fun `when indexOffences is called then ppud client is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(referenceService.retrieveIndexOffences()).willReturn(values)

      val result = controller.indexOffences()

      then(referenceService).should().retrieveIndexOffences()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }
}
