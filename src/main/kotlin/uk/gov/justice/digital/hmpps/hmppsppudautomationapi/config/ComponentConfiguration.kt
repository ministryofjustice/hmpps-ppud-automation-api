package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope
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
  fun dateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @Bean
  fun dateTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
}
