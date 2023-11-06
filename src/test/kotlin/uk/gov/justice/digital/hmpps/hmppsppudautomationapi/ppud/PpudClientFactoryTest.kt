package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PpudClientFactoryTest {

  @Test
  fun `when create is called then PPUD client is created`() {
    runBlocking {
      val factory = PpudClientFactory("https://ppud.example.com")

      val result = factory.create()

      assertThat(result).isNotNull()
    }
  }
}
