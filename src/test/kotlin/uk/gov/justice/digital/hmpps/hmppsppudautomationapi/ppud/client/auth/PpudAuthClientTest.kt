package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.auth

import ch.qos.logback.classic.Level
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InOrder
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriver.Navigation
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.auth.PpudAuthConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.auth.ppudAuthConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.ppudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.PpudOperationClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomBoolean
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util.findLogAppender
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
class PpudAuthClientTest {

  @InjectMocks
  private lateinit var authorisationClient: PpudAuthClient

  // We make these spies so that @InjectMocks will take it; we aren't
  // going to spy on them, as we just have them for the getters
  @Spy
  private val ppudAuthConfig: PpudAuthConfig = ppudAuthConfig()

  @Spy
  private val ppudClientConfig: PpudClientConfig = ppudClientConfig()

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var loginPage: LoginPage

  @Mock
  private lateinit var searchPage: SearchPage

  @Mock
  private lateinit var operationClient: PpudOperationClient

  private val logAppender = findLogAppender(PpudAuthClient::class.java)

  @Test
  fun `successfully logs in as admin and out before and after (respectively) running the given operation successfully`() {
    runTest {
      successfulLoginAndOutAndOperation(true)
    }
  }

  @Test
  fun `successfully logs in as regular user and out before and after (respectively) running the given operation successfully`() {
    runTest {
      successfulLoginAndOutAndOperation(false)
    }
  }

  @Test
  fun `successfully logs in as admin and out before and after (respectively) running the given operation unsuccessfully, bubbling up the exception`() {
    runTest {
      successfulLoginAndOutAndUnsuccessfulOperation(true)
    }
  }

  @Test
  fun `successfully logs in as regular user and out before and after (respectively) running the given operation unsuccessfully, bubbling up the exception`() {
    runTest {
      successfulLoginAndOutAndUnsuccessfulOperation(false)
    }
  }

  @Test
  fun `successfully logs in as admin before and unsuccessfully logs out after running the given operation successfully`() {
    runTest {
      successfulLoginAndUnsuccessfulLogoutAndSuccessfulOperation(true)
    }
  }

  @Test
  fun `successfully logs in as regular user before and unsuccessfully logs out after running the given operation successfully`() {
    runTest {
      successfulLoginAndUnsuccessfulLogoutAndSuccessfulOperation(false)
    }
  }

  @Test
  fun `successfully logs in as admin before and unsuccessfully logs out after running the given operation unsuccessfully, bubbling up the exception`() {
    runTest {
      successfulLoginAndUnsuccessfulLogoutAndUnsuccessfulOperation(true)
    }
  }

  @Test
  fun `successfully logs in as regular user before and unsuccessfully logs out after running the given operation unsuccessfully, bubbling up the exception`() {
    runTest {
      successfulLoginAndUnsuccessfulLogoutAndUnsuccessfulOperation(false)
    }
  }

  private suspend fun successfulLoginAndOutAndOperation(asAdmin: Boolean) {
    // given
    val retryOnFailure = randomBoolean()
    val operation: Supplier<Any> = mock<Supplier<Any>>()

    val urlPath: String = randomString()
    given(loginPage.urlPath).willReturn(urlPath)
    val navigation: Navigation = mock()
    given(driver.navigate()).willReturn(navigation)

    val any = Any()
    given(operationClient.invoke(retryOnFailure, operation)).willReturn(any)

    // when
    val actualResult = authorisationClient.performLoggedInOperation(asAdmin, retryOnFailure, operation)

    // then
    assertThat(actualResult).isSameAs(any)

    val inOrder: InOrder = inOrder(navigation, loginPage, searchPage, operationClient)
    verifySuccessfulLogin(inOrder, navigation, urlPath, asAdmin)
    inOrder.verify(operationClient).invoke(retryOnFailure, operation)
    verifySuccessfulLogout(inOrder, navigation)
  }

  private suspend fun successfulLoginAndOutAndUnsuccessfulOperation(asAdmin: Boolean) {
    // given
    val retryOnFailure = randomBoolean()
    val operation: Supplier<Any> = mock<Supplier<Any>>()

    val urlPath: String = randomString()
    given(loginPage.urlPath).willReturn(urlPath)
    val navigation: Navigation = mock()
    given(driver.navigate()).willReturn(navigation)

    val exception = RuntimeException()
    given(operationClient.invoke(retryOnFailure, operation)).willThrow(exception)

    // when
    val actualException = assertThrows<RuntimeException> {
      authorisationClient.performLoggedInOperation(asAdmin, retryOnFailure, operation)
    }

    // then
    assertThat(actualException).isSameAs(exception)

    val inOrder: InOrder = inOrder(navigation, loginPage, searchPage, operationClient)
    verifySuccessfulLogin(inOrder, navigation, urlPath, asAdmin)
    inOrder.verify(operationClient).invoke(retryOnFailure, operation)
    verifySuccessfulLogout(inOrder, navigation)
  }

  private suspend fun successfulLoginAndUnsuccessfulLogoutAndSuccessfulOperation(asAdmin: Boolean) {
    // given
    val retryOnFailure = randomBoolean()
    val operation: Supplier<Any> = mock<Supplier<Any>>()

    val urlPath: String = randomString()
    given(loginPage.urlPath).willReturn(urlPath)
    val navigation: Navigation = mock()
    given(driver.navigate()).willReturn(navigation)
    mockForSuccessfulLogin(navigation, urlPath)
    // We give it a random string as the message to check it is indeed being logged,
    // as the log appender doesn't give us access to the logged exception
    val exceptionMessage: String = randomString()
    given(navigation.to("${ppudClientConfig.url}/logout.aspx")).willThrow(RuntimeException(exceptionMessage))

    val any = Any()
    given(operationClient.invoke(retryOnFailure, operation)).willReturn(any)

    // when
    val actualResult = authorisationClient.performLoggedInOperation(asAdmin, retryOnFailure, operation)

    // then
    assertThat(actualResult).isSameAs(any)

    val inOrder: InOrder = inOrder(navigation, loginPage, searchPage, operationClient)
    verifySuccessfulLogin(inOrder, navigation, urlPath, asAdmin)
    inOrder.verify(operationClient).invoke(retryOnFailure, operation)
    verifyUnsuccessfulLogout(inOrder, navigation, exceptionMessage)
  }

  private suspend fun successfulLoginAndUnsuccessfulLogoutAndUnsuccessfulOperation(asAdmin: Boolean) {
    // given
    val retryOnFailure = randomBoolean()
    val operation: Supplier<Any> = mock<Supplier<Any>>()

    val urlPath: String = randomString()
    given(loginPage.urlPath).willReturn(urlPath)
    val navigation: Navigation = mock()
    given(driver.navigate()).willReturn(navigation)
    mockForSuccessfulLogin(navigation, urlPath)
    val exceptionMessage: String = mockForUnsuccessfulLogout(navigation)


    val exception = RuntimeException()
    given(operationClient.invoke(retryOnFailure, operation)).willThrow(exception)

    // when
    val actualException = assertThrows<RuntimeException> {
      authorisationClient.performLoggedInOperation(asAdmin, retryOnFailure, operation)
    }

    // then
    assertThat(actualException).isSameAs(exception)

    val inOrder: InOrder = inOrder(navigation, loginPage, searchPage, operationClient)
    verifySuccessfulLogin(inOrder, navigation, urlPath, asAdmin)
    inOrder.verify(operationClient).invoke(retryOnFailure, operation)
    verifyUnsuccessfulLogout(inOrder, navigation, exceptionMessage)
  }

  private fun mockForSuccessfulLogin(navigation: Navigation, urlPath: String) {
    // For some reason using willDoNothing().given(...) isn't working - mockito complains of
    // unfinished stubbing :( - so we stub the method to do nothing instead
    given(navigation.to("${ppudClientConfig.url}$urlPath")).will { }
  }

  private fun mockForUnsuccessfulLogout(navigation: Navigation): String {
    // We give it a random string as the message to check it is indeed being logged,
    // as the log appender doesn't give us access to the logged exception
    val exceptionMessage: String = randomString()
    given(navigation.to("${ppudClientConfig.url}/logout.aspx")).willThrow(RuntimeException(exceptionMessage))
    return exceptionMessage
  }

  private fun verifySuccessfulLogin(
    inOrder: InOrder, navigation: Navigation, urlPath: String, asAdmin: Boolean,
  ) {
    inOrder.verify(navigation).to("${ppudClientConfig.url}$urlPath")
    inOrder.verify(loginPage).verifyOn()
    val username = if (asAdmin) ppudAuthConfig.adminUsername else ppudAuthConfig.username
    val password = if (asAdmin) ppudAuthConfig.adminPassword else ppudAuthConfig.password
    inOrder.verify(loginPage).login(username, password)
    inOrder.verify(loginPage).throwIfInvalid()
    inOrder.verify(searchPage).verifyOn()
  }

  private fun verifySuccessfulLogout(inOrder: InOrder, navigation: Navigation) {
    inOrder.verify(navigation).to("${ppudClientConfig.url}/logout.aspx")
  }

  private fun verifyUnsuccessfulLogout(
    inOrder: InOrder,
    navigation: Navigation,
    exceptionMessage: String,
  ) {
    inOrder.verify(navigation).to("${ppudClientConfig.url}/logout.aspx")

    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.ERROR)
        assertThat(message).isEqualTo("Error attempting to log out of PPUD")
        assertThat(throwableProxy.message).isEqualTo(exceptionMessage)
      }
    }
  }

}