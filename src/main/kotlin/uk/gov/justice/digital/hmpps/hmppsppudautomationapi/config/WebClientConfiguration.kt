package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

@Configuration
class WebClientConfiguration(
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.health.timeout}") private val ppudHealthTimeout: Long,
  @Value("\${document-management.api.url}") private val documentManagementApiRootUri: String,
  @Value("\${document-management.client.timeout}") private val documentManagementTimeout: Long,
  @Value("\${document-management.health.timeout}") private val documentManagementHealthTimeout: Long,
  @Value("\${document-management.client.headers.serviceName}") private val documentManagementHeaderServiceName: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun <T> Mono<T>.withRetry(): Mono<T> = this
      .retryWhen(
        Retry.backoff(2, Duration.ofMillis(500))
          .filter(::shouldBeRetried)
          .doBeforeRetry(::logRetrySignal)
          .onRetryExhaustedThrow { _, retrySignal ->
            retrySignal.failure()
          },
      )

    private fun logRetrySignal(retrySignal: Retry.RetrySignal) {
      val exception = retrySignal.failure()?.cause ?: retrySignal.failure()
      log.error(
        "Exception occurred but operation will be retried. Total retries: ${retrySignal.totalRetries()}",
        exception,
      )
    }

    private val transientStatusCodes: Set<Int> = setOf(
      HttpStatus.REQUEST_TIMEOUT.value(),
      HttpStatus.BAD_GATEWAY.value(),
      HttpStatus.SERVICE_UNAVAILABLE.value(),
      HttpStatus.GATEWAY_TIMEOUT.value(),
      // Client disconnect as reported by Kibana
      499,
    )

    private fun shouldBeRetried(ex: Throwable): Boolean = ex is ClientTimeoutException ||
      ex is TimeoutException ||
      ex is WebClientRequestException ||
      (ex is WebClientResponseException && transientStatusCodes.contains(ex.statusCode.value()))
  }

  @Bean
  fun authorizedClientManagerAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun ppudHealthCheckWebClient(): WebClient {
    val sslContext = SslContextBuilder
      .forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE)
      .build()
    val client: HttpClient = HttpClient.create()
      .secure { t -> t.sslContext(sslContext) }
      .responseTimeout(Duration.ofSeconds(ppudHealthTimeout))
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(client))
      .baseUrl(ppudUrl)
      .build()
  }

  @Bean
  fun documentManagementHealthCheckWebClient(): WebClient {
    val client: HttpClient = HttpClient.create()
      .responseTimeout(Duration.ofSeconds(documentManagementHealthTimeout))
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(client))
      .baseUrl(documentManagementApiRootUri)
      .build()
  }

  @Bean
  fun documentManagementApiWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    val withServiceNameHeader = builder.defaultHeader("Service-Name", documentManagementHeaderServiceName)
    return getOAuthWebClient(authorizedClientManager, withServiceNameHeader, documentManagementApiRootUri, "document-management")
  }

  @Bean
  fun documentManagementApiClient(@Qualifier("documentManagementApiWebClientAppScope") webClient: WebClient): DocumentManagementClient = DocumentManagementClient(webClient, documentManagementTimeout)

  private fun getOAuthWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
    rootUri: String,
    @Suppress("SameParameterValue") registrationId: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(registrationId)
    return builder.baseUrl(rootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }
}
