package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class OffenceClientTest {

  @InjectMocks
  private lateinit var client: OffenceClient

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var offencePage: OffencePage

  @Mock
  private lateinit var navigation: Navigation

  @Test
  fun `extracts the details of the linked offence`() {
    // given
    val offenceUrl = randomString()
    given(driver.navigate()).willReturn(navigation)

    val expectedOffence = offence()
    given(offencePage.extractOffenceDetails()).willReturn(expectedOffence)

    // when
    val actualOffence = client.getOffence(offenceUrl)

    // then
    assertThat(actualOffence).isEqualTo(expectedOffence)
    val inOrder = inOrder(navigation, offencePage)
    then(navigation).should(inOrder).to("${ppudClientConfig.url}$offenceUrl")
    then(offencePage).should(inOrder).extractOffenceDetails()
  }

}