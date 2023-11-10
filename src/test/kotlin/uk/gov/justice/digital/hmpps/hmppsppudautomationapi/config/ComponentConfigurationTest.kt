package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.openqa.selenium.SessionNotCreatedException
import java.lang.reflect.InvocationTargetException

class ComponentConfigurationTest {

  @Test
  fun `given path to firefox binary env var when configuring WebDriver then binary is set`() {
    val binary = "/path/to/invalidBinary"

    val configuration = ComponentConfiguration()
    val ex = assertThrows<Exception> {
      configuration.webDriver(headless = true, binary = binary)
    }
    val cause = ex.cause as InvocationTargetException
    val actualException = cause.cause as SessionNotCreatedException
    assertTrue(
      actualException.message!!.contains("binary is not a Firefox executable"),
      "Exception message is '${actualException.message}'",
    )
  }

  @Test
  fun `given no path to firefox binary env var when configuring WebDriver then default binary is used`() {
    val configuration = ComponentConfiguration()
    assertDoesNotThrow {
      configuration.webDriver(headless = true, binary = null)
    }
  }
}
