package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Component("ppud")
class PpudHealth(
  private val webClient: WebClient,
  @Value("\${ppud.url}") private val endpointUrl: String,
  @Value("\${ppud.health.port}") private val port: Int,
  @Value("\${ppud.health.path}") private val path: String,
  @Value("\${ppud.health.timeoutSeconds}") private val timeoutSeconds: Long = 1,
) : HealthIndicator {
  override fun health(): Health? {
    val healthUrl = "$endpointUrl:$port$path"
    val timeout = Duration.ofSeconds(timeoutSeconds)
    val result =
      try {
        webClient.get()
          .uri(healthUrl)
          .retrieve()
          .toBodilessEntity()
          .block(timeout)
          .let { upWithStatus(it!!.statusCode) }
      } catch (e: WebClientResponseException) {
        downWithResponseBody(e)
      } catch (ex: Exception) {
        downWithException(ex)
      }

    return result
  }

  private fun downWithException(it: Exception): Health =
    Health.down().withException(it).build()

  private fun downWithResponseBody(it: WebClientResponseException): Health =
    Health.down().withException(it).withBody(it.responseBodyAsString).withHttpStatus(it.statusCode).build()

  private fun upWithStatus(statusCode: HttpStatusCode): Health = Health.up().withHttpStatus(statusCode).build()

  private fun Health.Builder.withHttpStatus(statusCode: HttpStatusCode) =
    this.withDetail("statusCode", statusCode.toString())

  private fun Health.Builder.withBody(body: String) = this.withDetail("body", body)
}
