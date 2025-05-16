package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.err

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.PpudErrorException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class PpudErrorHandlerClientTest {

  @InjectMocks
  private lateinit var handler: PpudErrorHandlerClient

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var errorPage: ErrorPage

  @Test
  fun `re-raises wrapped in PpudErrorException if redirected to the error page`() {
    // given
    given(errorPage.isShown()).willReturn(true)
    val errorDetails = randomString()
    given(errorPage.extractErrorDetails()).willReturn(errorDetails)

    val exception = WebDriverException()
    val expectedException = PpudErrorException(
      "PPUD has displayed an error. Details are: '$errorDetails'",
      exception,
    )

    // when
    val actualException = handler.handleException(exception)

    // then
    assertThat(actualException).usingRecursiveComparison().isEqualTo(expectedException)
  }

  @Test
  fun `re-raises wrapped in AutomationException if not redirected to the error page`() {
    // given
    given(errorPage.isShown()).willReturn(false)
    val currentUrl = randomString()
    given(driver.currentUrl).willReturn(currentUrl)

    val exception = WebDriverException()
    val expectedException = AutomationException(
      "Exception occurred when performing PPUD operation. Current URL is '$currentUrl'",
      exception,
    )

    // when
    val actualException = handler.handleException(exception)

    // then
    assertThat(actualException).usingRecursiveComparison().isEqualTo(expectedException)
  }
}
