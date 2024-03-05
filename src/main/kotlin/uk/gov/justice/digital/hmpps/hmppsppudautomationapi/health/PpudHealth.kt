package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import io.netty.handler.timeout.TimeoutException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component("ppud")
class PpudHealth(
  @Qualifier(value = "healthCheckWebClient") private val webClient: WebClient,
  @Value("\${ppud.health.path}") private val path: String,
) : HealthIndicator {
  override fun health(): Health? {
    val result =
      try {
        webClient.get()
          .uri(path)
          .retrieve()
          .toEntity(String::class.java)
          .doOnError {
            if (it.cause is TimeoutException) throw it.cause as TimeoutException
          }
          .flatMap { upWithStatus(it) }
          .block()
      } catch (e: WebClientResponseException) {
        downWithResponseBody(e)
      } catch (ex: Exception) {
        downWithException(ex)
      }

    return result
  }
}

private fun upWithStatus(it: ResponseEntity<String>): Mono<Health> =
  Mono.just(Health.up().withHttpStatus(it.statusCode).build())

private fun downWithException(it: Exception): Health {
  return Health.down().withException(it).build()
}

private fun downWithResponseBody(it: WebClientResponseException): Health {
  return Health.down().withException(it).withBody(it.responseBodyAsString).withHttpStatus(it.statusCode)
    .build()
}

private fun Health.Builder.withHttpStatus(status: HttpStatusCode) = this.withDetail("status", status)

private fun Health.Builder.withBody(body: String) = this.withDetail("body", body)
