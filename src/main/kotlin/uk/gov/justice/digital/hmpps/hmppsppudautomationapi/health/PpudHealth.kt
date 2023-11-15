package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.health

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.time.Duration

@Component("ppud")
@RequestScope
class PpudHealth(
  private val driver: WebDriver,
  @Value("\${ppud.url}") private val endpointUrl: String,
  @Value("\${ppud.health.path}") private val path: String,
  @Value("\${ppud.health.pageTitle}") private val pageTitle: String,
  @Value("\${ppud.health.timeoutSeconds}") private val timeoutSeconds: Long = 1,
) : HealthIndicator {
  override fun health(): Health? {
    val timeout = Duration.ofSeconds(timeoutSeconds)
    driver.manage().timeouts().pageLoadTimeout(timeout)
    val result =
      try {
        driver.get("$endpointUrl$path")
        WebDriverWait(driver, Duration.ofSeconds(2))
          .until(ExpectedConditions.titleIs(pageTitle))
        Health.up().build()
      } catch (ex: Exception) {
        Health.down().withException(ex).build()
      }

    return result
  }
}
