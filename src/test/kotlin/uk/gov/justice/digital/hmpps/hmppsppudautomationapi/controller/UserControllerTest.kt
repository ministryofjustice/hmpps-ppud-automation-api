package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.controller

import jakarta.validation.ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.then
import org.springframework.http.HttpStatus
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
  fun `when search active users by userFullName and userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = randomString("userName")
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchActiveUsers(fullName, userName)).willReturn(users)

      val result = controller.searchActiveUsers(request)

      then(ppudClient).should().searchActiveUsers(fullName, userName)
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(users, result.body?.results)
    }
  }

  @Test
  fun `when search active users by userFullName and null userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = randomString("fullName")
      val userName = null
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchActiveUsers(fullName, userName)).willReturn(users)

      val result = controller.searchActiveUsers(request)

      then(ppudClient).should().searchActiveUsers(fullName, userName)
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(users, result.body?.results)
    }
  }

  @Test
  fun `when search active users by null userFullName and userName is called then PPUD client is invoked and results returned`() {
    runBlocking {
      val fullName = null
      val userName = randomString("userName")
      val request = generateUserSearchRequest(fullName, userName)
      val users = listOf(
        generatePpudUser(),
        generatePpudUser(),
      )
      given(ppudClient.searchActiveUsers(fullName, userName)).willReturn(users)

      val result = controller.searchActiveUsers(request)

      then(ppudClient).should().searchActiveUsers(fullName, userName)
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(users, result.body?.results)
    }
  }

  @Test
  fun `when search active users by null userFullName and null userName is called then ValidationException is thrown`() {
    runBlocking {
      val fullName = null
      val userName = null
      val request = generateUserSearchRequest(fullName, userName)

      assertThrows<ValidationException> {
        var response = controller.searchActiveUsers(request)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode.value())
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

      val result = controller.getActiveUsers()

      then(ppudClient).should().retrieveActiveUsers()
      assertEquals(HttpStatus.OK.value(), result.statusCode.value())
      assertEquals(users, result.body?.results)
    }
  }
}
