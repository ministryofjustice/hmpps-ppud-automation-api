package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.response

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserResponseTest {

  private lateinit var response: UserResponse

  @BeforeEach
  fun beforeEach() {
    response = UserResponse()
  }

  @Test
  fun `should expose 'results' of type List of PpudUser (initialised to empty list)`() {
    assertEquals(response.results, emptyList<UserResponse>())
  }
}
