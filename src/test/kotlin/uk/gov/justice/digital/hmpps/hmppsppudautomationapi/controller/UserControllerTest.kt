package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import jakarta.validation.ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.OperationalPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generatePpudUser
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.generateUserSearchRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class UserControllerTest {

  @Mock
  lateinit var ppudClient: OperationalPpudClient

  private lateinit var controller: UserController

  @BeforeEach
  fun beforeEach() {
    controller = UserController(ppudClient)
  }

  @Test
  fun `when get users by userFullName and userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = randomString("userName")
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchUsers(fullName, userName)).willReturn(users)

      controller.search(request)

      then(ppudClient).should().searchUsers(fullName, userName)
    }
  }

  @Test
  fun `when get users by userFullName and null userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = null
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchUsers(fullName, userName)).willReturn(users)

      controller.search(request)

      then(ppudClient).should().searchUsers(fullName, userName)
    }
  }

  @Test
  fun `when get users by null userFullName and userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = null
      val userName = randomString("userName")
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchUsers(fullName, userName)).willReturn(users)

      controller.search(request)

      then(ppudClient).should().searchUsers(fullName, userName)
    }
  }

  @Test
  fun `when get users by null userFullName and null userName is called then ValidationException is thrown`() {
    runBlocking {
      val fullName = null
      val userName = null
      val request = generateUserSearchRequest(fullName, userName)

      assertThrows<ValidationException> {
        controller.search(request)
      }
    }
  }

  @Test
  fun `when get active users is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.retrieveActiveUsers()).willReturn(users)

      controller.getActiveUsers()

      then(ppudClient).should().retrieveActiveUsers()
    }
  }
}
