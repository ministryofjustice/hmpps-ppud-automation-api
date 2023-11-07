package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope

@Configuration
class ComponentConfiguration {

  @Bean
  @RequestScope
  fun webDriver(): WebDriver {
    val options = FirefoxOptions()
    return WebDriverManager.firefoxdriver().capabilities(options).create()
  }
}
