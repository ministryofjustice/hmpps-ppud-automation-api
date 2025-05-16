package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.release

import ch.qos.logback.classic.Level
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.createdOrUpdatedRelease
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.createOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth.PpudAuthClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.release.ReleaseClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.findLogAppender
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
internal class ReleaseServiceTest {

  @InjectMocks
  private lateinit var releaseService: ReleaseService

  @Mock
  private lateinit var authClient: PpudAuthClient

  @Mock
  private lateinit var releaseClient: ReleaseClient

  private val logAppender = findLogAppender(ReleaseService::class.java)

  @Test
  fun `creates or updates a release`() {
    runTest {
      // given
      val offenderId = randomString()
      val sentenceId = randomString()
      val request = createOrUpdateReleaseRequest()
      val methodCaptor = argumentCaptor<Supplier<CreatedOrUpdatedRelease>>()

      val expectedCreatedOrUpdatedRelease = createdOrUpdatedRelease()
      given(
        authClient.performLoggedInOperation(
          eq(false),
          eq(true),
          any<Supplier<CreatedOrUpdatedRelease>>(),
        ),
      ).willReturn(
        expectedCreatedOrUpdatedRelease,
      )

      // when
      val actualCreatedOrUpdatedRelease = releaseService.createOrUpdateRelease(offenderId, sentenceId, request)

      // then
      assertThat(actualCreatedOrUpdatedRelease).isEqualTo(expectedCreatedOrUpdatedRelease)

      then(authClient).should().performLoggedInOperation(eq(false), eq(true), methodCaptor.capture())

      then(releaseClient).shouldHaveNoInteractions()
      given(releaseClient.createOrUpdateRelease(offenderId, sentenceId, request)).willReturn(
        expectedCreatedOrUpdatedRelease,
      )
      val method: Supplier<CreatedOrUpdatedRelease> = methodCaptor.firstValue
      method.get()
      then(releaseClient).should().createOrUpdateRelease(offenderId, sentenceId, request)

      with(logAppender.list) {
        assertThat(size).isEqualTo(1)
        with(get(0)) {
          assertThat(level).isEqualTo(Level.INFO)
          assertThat(message).isEqualTo("Creating/updating release in PPUD Client")
        }
      }
    }
  }
}
