package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope

@Configuration
class ComponentConfiguration {

  @Bean
  @RequestScope
  fun webDriver(@Value("\${automation.headless}") headless: Boolean): WebDriver {
    val options = FirefoxOptions()
    if (headless) {
      options.addArguments("-headless")
    }
    return WebDriverManager.firefoxdriver().capabilities(options).create()
  }
}
