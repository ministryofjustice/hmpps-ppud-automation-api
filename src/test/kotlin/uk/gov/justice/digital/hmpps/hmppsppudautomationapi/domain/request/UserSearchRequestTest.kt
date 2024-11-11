package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

class UserSearchRequestTest {

  @Test
  fun `given fullName and userName return true`() {
    val request = UserSearchRequest(
      randomString("fullName"),
      randomString("userName"),
    )
    assertEquals(request.containsCriteria, true)
  }

  @Test
  fun `given fullName and null userName return true`() {
    val request = UserSearchRequest(
      randomString("fullName"),
      null,
    )
    assertEquals(request.containsCriteria, true)
  }

  @Test
  fun `given null fullName and userName return true`() {
    val request = UserSearchRequest(
      null,
      randomString("userName"),
    )
    assertEquals(request.containsCriteria, true)
  }

  @Test
  fun `given null fullName and null userName return false`() {
    val request = UserSearchRequest(
      null,
      null,
    )
    assertEquals(request.containsCriteria, false)
  }
}
