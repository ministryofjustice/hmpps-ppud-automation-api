package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.PpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

class CaseworkerAdminPageTest {

  private lateinit var caseworkerAdminPath: CaseworkerAdminPage

  @BeforeEach
  fun beforeEach() {
    caseworkerAdminPath = mock(CaseworkerAdminPage::class.java)
  }

  @Test
  fun `searches with criteria should return list of users`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = randomString("userName")
      val users = emptyList<PpudUser>()

      given(caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)).willReturn(users)

      val result = caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)

      then(caseworkerAdminPath).should().extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with null fullName and null userName criteria should return list of users`() {
    runBlocking {
      val fullName = null
      val userName = null
      val users = emptyList<PpudUser>()

      given(caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)).willReturn(users)

      val result = caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)

      then(caseworkerAdminPath).should().extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with null fullName and non-null userName criteria should return list of users`() {
    runBlocking {
      val fullName = null
      val userName = randomString("userName")
      val users = emptyList<PpudUser>()

      given(caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)).willReturn(users)

      val result = caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)

      then(caseworkerAdminPath).should().extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `searches with null userName and non-null fullName critera should return list of users`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = null
      val users = emptyList<PpudUser>()

      given(caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)).willReturn(users)

      val result = caseworkerAdminPath.extractActiveUsersByCriteria(fullName, userName)

      then(caseworkerAdminPath).should().extractActiveUsersByCriteria(fullName, userName)
      assertEquals(users, result)
    }
  }

  @Test
  fun `extract active users should return list of users`() {
    runBlocking {
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )

      given(caseworkerAdminPath.extractActiveUsers()).willReturn(users)

      val result = caseworkerAdminPath.extractActiveUsers()

      then(caseworkerAdminPath).should().extractActiveUsers()
      assertEquals(users, result)
    }
  }
}
