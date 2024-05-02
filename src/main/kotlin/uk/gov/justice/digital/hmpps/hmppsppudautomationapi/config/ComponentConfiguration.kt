package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.AdminPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.EditLookupsPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.ErrorPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.ReferenceService
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.ReferenceServiceImpl
import java.time.format.DateTimeFormatter
import java.util.UUID

@Configuration
internal class ComponentConfiguration {

  companion object {
    const val FIREFOX_PREFERENCE_BLOCK = 2

    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    // Limit Selenium logging so that we do not get the log entries mentioned in
    // https://github.com/SeleniumHQ/selenium/issues/13096
    java.util.logging.Logger.getLogger("").level = java.util.logging.Level.SEVERE
  }

  @Bean
  @Primary
  fun referenceService(
    referenceDataPpudClient: ReferenceDataPpudClient,
    cacheManager: CacheManager,
  ): ReferenceService {
    return ReferenceServiceImpl(referenceDataPpudClient, cacheManager)
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  fun scheduledReferenceService(
    @Qualifier("scheduledReferenceDataPpudClient") scheduledReferenceDataPpudClient: ReferenceDataPpudClient,
    cacheManager: CacheManager,
  ): ReferenceService {
    return ReferenceServiceImpl(scheduledReferenceDataPpudClient, cacheManager)
  }

  @Bean
  @Primary
  fun referenceDataPpudClient(
    @Value("\${ppud.url}") ppudUrl: String,
    @Value("\${ppud.username}") ppudUsername: String,
    @Value("\${ppud.password}") ppudPassword: String,
    @Value("\${ppud.admin.username}") ppudAdminUsername: String,
    @Value("\${ppud.admin.password}") ppudAdminPassword: String,
    driver: WebDriver,
    errorPage: ErrorPage,
    loginPage: LoginPage,
    searchPage: SearchPage,
    @Value("\${ppud.reference.valueToExclude}") valueToExclude: String,
    adminPage: AdminPage,
    editLookupsPage: EditLookupsPage,
  ): ReferenceDataPpudClient {
    return ReferenceDataPpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      ppudAdminUsername,
      ppudAdminPassword,
      driver,
      errorPage,
      loginPage,
      searchPage,
      valueToExclude,
      adminPage,
      editLookupsPage,
    )
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  fun scheduledReferenceDataPpudClient(
    @Value("\${ppud.url}") ppudUrl: String,
    @Value("\${ppud.username}") ppudUsername: String,
    @Value("\${ppud.password}") ppudPassword: String,
    @Value("\${ppud.admin.username}") ppudAdminUsername: String,
    @Value("\${ppud.admin.password}") ppudAdminPassword: String,
    @Qualifier("scheduledWebDriver") driver: WebDriver,
    @Value("\${ppud.reference.valueToExclude}") valueToExclude: String,
    dateFormatter: DateTimeFormatter,
  ): ReferenceDataPpudClient {
    return ReferenceDataPpudClient(
      ppudUrl,
      ppudUsername,
      ppudPassword,
      ppudAdminUsername,
      ppudAdminPassword,
      driver,
      ErrorPage(driver, PageHelper(driver, dateFormatter)),
      LoginPage(driver),
      SearchPage(driver),
      valueToExclude,
      AdminPage(driver),
      EditLookupsPage(driver),
    )
  }

  @Bean(destroyMethod = "quit")
  @Primary
  @RequestScope
  fun webDriver(
    @Value("\${automation.headless}") headless: Boolean,
    @Value("\${automation.firefox.binary}") binary: String?,
  ): WebDriver {
    return createWebDriver(binary, headless)
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  fun scheduledWebDriver(
    @Value("\${automation.headless}") headless: Boolean,
    @Value("\${automation.firefox.binary}") binary: String?,
  ): WebDriver {
    return createWebDriver(binary, headless)
  }

  @Bean
  fun dateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @Bean
  fun dateTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

  private fun createWebDriver(binary: String?, headless: Boolean): WebDriver {
    val options = FirefoxOptions()
    if (!binary.isNullOrBlank()) {
      log.info("Setting Firefox binary to '$binary'")
      options.setBinary(binary)
    }

    if (headless) {
      options.addArguments("-headless")
    }

    options.addPreference("permissions.default.image", FIREFOX_PREFERENCE_BLOCK)

    return WebDriverManager
      .firefoxdriver()
      .capabilities(options)
      .avoidShutdownHook()
      .create()
  }

  @Bean
  fun uuidProvider(): () -> UUID {
    return { UUID.randomUUID() }
  }
}
