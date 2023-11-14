package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit

class PpudHealthTest {

  private val ppudUrl = "http://localhost"

  private val ppudPort = 8100

  private val healthPath = "/login.aspx"

  private val ppudMock: ClientAndServer = startClientAndServer(ppudPort)

  private val webClient: WebClient = WebClient.create()

  private val ppudHealth = PpudHealth(webClient, ppudUrl, ppudPort, healthPath)

  @AfterEach
  fun afterEach() {
    ppudMock.stop()
  }

  @Test
  fun `given ppud is ok when called then return UP`() {
    ppudMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .respond(
        HttpResponse.response()
          .withContentType(MediaType.HTML_UTF_8)
          .withStatusCode(HttpStatusCode.OK_200.code())
          .withBody("<html><body>Some html</body></html>"),
      )

    val result = ppudHealth.health()

    assertEquals(Status.UP, result?.status)
    assertEquals("200 OK", result?.details?.get("statusCode"))
  }

  @Test
  fun `given ppud is returning 5xx responses when called then return DOWN`() {
    val responseBody = "{ \"error\": \"5xx error occurred\" }"
    setupMockToErrorWith(HttpStatusCode.INTERNAL_SERVER_ERROR_500, responseBody)

    val result = ppudHealth.health()

    assertEquals(Status.DOWN, result?.status)
    assertEquals("500 INTERNAL_SERVER_ERROR", result?.details?.get("statusCode"))
    assertEquals(responseBody, result?.details?.get("body"))
  }

  @Test
  fun `given ppud is returning 4xx responses when called then return DOWN`() {
    val responseBody = "{ \"error\": \"4xx error occurred\" }"
    setupMockToErrorWith(HttpStatusCode.NOT_FOUND_404, responseBody)

    val result = ppudHealth.health()

    assertEquals(Status.DOWN, result?.status)
    assertEquals("404 NOT_FOUND", result?.details?.get("statusCode"))
    assertEquals(responseBody, result?.details?.get("body"))
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

  private fun setupMockToErrorWith(statusCode: HttpStatusCode, responseBody: String) {
    ppudMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .respond(
        HttpResponse.response()
          .withContentType(MediaType.APPLICATION_JSON_UTF_8)
          .withBody(responseBody)
          .withStatusCode(statusCode.code()),
      )
  }
}
