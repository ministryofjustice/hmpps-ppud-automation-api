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
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.PpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class ReferenceControllerTest {

  @Mock
  lateinit var ppudClient: PpudClient

  private lateinit var controller: ReferenceController

  @BeforeEach
  fun beforeEach() {
    controller = ReferenceController(ppudClient)
  }

  @Test
  fun `when establishments is called then ppud client is invoked and results returned`() {
    runBlocking {
      val values = listOf(randomString(), randomString(), randomString())
      given(ppudClient.retrieveLookupValues()).willReturn(values)

      val result = controller.establishments()

      then(ppudClient).should().retrieveLookupValues()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(values, result.body?.values)
    }
  }
}
