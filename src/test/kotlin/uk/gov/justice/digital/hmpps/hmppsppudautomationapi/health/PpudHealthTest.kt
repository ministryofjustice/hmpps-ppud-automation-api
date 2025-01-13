package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.Delay
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import org.mockserver.model.MediaType
import org.springframework.boot.actuate.health.Status
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

class PpudHealthTest {

  private val ppudPort = 8100

  private val ppudUrl = "http://localhost:$ppudPort"

  private val healthPath = "/login.aspx"

  private val pageTitle = "Mock Page"

  private val timeout = 5L

  private val ppudMock: ClientAndServer = startClientAndServer(ppudPort)

  private lateinit var ppudHealth: PpudHealth

  @BeforeEach
  fun beforeEach() {
    val webClient = setupWebClient()
    ppudHealth = PpudHealth(webClient, healthPath)
  }

  @AfterEach
  fun afterEach() {
    ppudMock.stop()
  }

  @Test
  fun `given ppud is ok when called then return UP`() {
    val responseBody = createResponseBody(pageTitle, "Successful page load")
    setupMockToRespondWith(HttpStatusCode.OK_200, responseBody)

    val result = ppudHealth.health()

    assertEquals(Status.UP, result?.status)
  }

  @Test
  fun `given ppud is returning 5xx responses when called then return DOWN`() {
    val responseBody = createResponseBody("Error", "5xx error occurred")
    setupMockToRespondWith(HttpStatusCode.INTERNAL_SERVER_ERROR_500, responseBody)

    val result = ppudHealth.health()

    assertEquals(Status.DOWN, result?.status)
  }

  @Test
  fun `given ppud is returning 4xx responses when called then return DOWN`() {
    val responseBody = createResponseBody("Error", "4xx error occurred")
    setupMockToRespondWith(HttpStatusCode.NOT_FOUND_404, responseBody)

    val result = ppudHealth.health()

    assertEquals(Status.DOWN, result?.status)
  }

  @Test
  fun `given ppud times out when called then return DOWN`() {
    ppudMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .error(HttpError().withDelay(Delay(TimeUnit.MINUTES, 1)))
    val result = ppudHealth.health()

    assertEquals(Status.DOWN, result?.status)
    val errorDetails = result?.details?.get("error").toString()
    assertTrue(errorDetails.contains("Timeout"), "Detail was '$errorDetails'")
  }

  private fun setupWebClient(): WebClient {
    val client: HttpClient = HttpClient.create()
      .responseTimeout(Duration.ofSeconds(timeout))
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(client))
      .baseUrl(ppudUrl)
      .build()
  }

  private fun createResponseBody(responsePageTitle: String, text: String): String = "<html><head><title>$responsePageTitle</title></head><body>$text</body></html>"

  private fun setupMockToRespondWith(statusCode: HttpStatusCode, responseBody: String) {
    ppudMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .respond(
        HttpResponse.response()
          .withStatusCode(statusCode.code())
          .withContentType(MediaType.HTML_UTF_8)
          .withBody(responseBody),
      )
  }
}
