package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.github.bonigarcia.wdm.WebDriverManager
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.format.DateTimeFormatter

@Configuration
class ComponentConfiguration {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  @RequestScope
  fun webDriver(
    @Value("\${automation.headless}") headless: Boolean,
    @Value("\${automation.firefox.binary}") binary: String?,
  ): WebDriver {
    val options = FirefoxOptions()
    if (!binary.isNullOrBlank()) {
      log.info("Setting Firefox binary to '$binary'")
      options.setBinary(binary)
    }

    if (headless) {
      options.addArguments("-headless")
    }

    return WebDriverManager.firefoxdriver().capabilities(options).create()
  }

  @Bean
  fun healthCheckWebClient(
    @Value("\${ppud.url}") ppudUrl: String,
    @Value("\${ppud.health.timeoutSeconds}") timeoutSeconds: Long = 5,
  ): WebClient {
    val sslContext = SslContextBuilder
      .forClient()
      .trustManager(InsecureTrustManagerFactory.INSTANCE)
      .build()
    val client: HttpClient = HttpClient.create()
      .secure { t -> t.sslContext(sslContext) }
      .responseTimeout(Duration.ofSeconds(timeoutSeconds))
    return WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(client))
      .baseUrl(ppudUrl)
      .build()
  }

  @Bean
  fun dateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @Bean
  fun dateTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
}
