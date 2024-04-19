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
import org.springframework.boot.actuate.health.Status
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

class DocumentManagementHealthTest {

  private val documentManagementPort = 8100

  private val documentManagementUrl = "http://localhost:$documentManagementPort"

  private val healthPath = "/health/ping"

  private val timeout = 5L

  private val documentManagementMock: ClientAndServer = startClientAndServer(documentManagementPort)

  private lateinit var documentManagementHealth: DocumentManagementHealth

  @BeforeEach
  fun beforeEach() {
    val webClient = setupWebClient()
    documentManagementHealth = DocumentManagementHealth(webClient)
  }

  @AfterEach
  fun afterEach() {
    documentManagementMock.stop()
  }

  @Test
  fun `given document management service is ok when called then return UP`() {
    setupMockToRespondWith(HttpStatusCode.OK_200)

    val result = documentManagementHealth.health()

    assertEquals(Status.UP, result?.status)
  }

  @Test
  fun `given document management service is returning 5xx responses when called then return DOWN`() {
    setupMockToRespondWith(HttpStatusCode.INTERNAL_SERVER_ERROR_500)

    val result = documentManagementHealth.health()

    assertEquals(Status.DOWN, result?.status)
  }

  @Test
  fun `given document management service is returning 4xx responses when called then return DOWN`() {
    setupMockToRespondWith(HttpStatusCode.NOT_FOUND_404)

    val result = documentManagementHealth.health()

    assertEquals(Status.DOWN, result?.status)
  }

  @Test
  fun `given document management service times out when called then return DOWN`() {
    documentManagementMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .error(HttpError().withDelay(Delay(TimeUnit.MINUTES, 1)))
    val result = documentManagementHealth.health()

    assertEquals(Status.DOWN, result?.status)
    val errorDetails = result?.details?.get("error").toString()
    assertTrue(errorDetails.contains("Timeout"), "Detail was '$errorDetails'")
  }

  private fun setupWebClient(): WebClient {
    val client: HttpClient = HttpClient.create()
      .responseTimeout(Duration.ofSeconds(timeout))
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(client))
      .baseUrl(documentManagementUrl)
      .build()
  }

  private fun setupMockToRespondWith(statusCode: HttpStatusCode) {
    documentManagementMock
      .`when`(HttpRequest.request().withPath(healthPath))
      .respond(
        HttpResponse.response()
          .withStatusCode(statusCode.code()),
      )
  }
}
