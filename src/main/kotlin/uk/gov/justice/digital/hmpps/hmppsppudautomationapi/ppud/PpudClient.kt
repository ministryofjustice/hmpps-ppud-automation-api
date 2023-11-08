package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud

import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.Offender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.LoginPage
import java.time.LocalDate

@Component
@RequestScope
internal class PpudClient(
  @Value("\${ppud.url}") private val ppudUrl: String,
  @Value("\${ppud.username}") private val ppudUsername: String,
  @Value("\${ppud.password}") private val ppudPassword: String,
  private val driver: WebDriver,
  private val loginPage: LoginPage,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun searchForOffender(
    croNumber: String?,
    nomsId: String?,
    familyName: String?,
    dateOfBirth: LocalDate?,
  ): List<Offender> {
    log.info("Searching in PPUD Client")

    login()

    return listOf(
      Offender(
        "1",
        croNumber ?: "",
        nomsId ?: "",
        "John",
        familyName ?: "Teal",
        dateOfBirth ?: LocalDate.now(),
      ),
    )
  }

  private suspend fun login() {
    driver.get("${ppudUrl}${loginPage.urlFragment}")
    loginPage.verifyOn()
    loginPage.login(ppudUsername, ppudPassword)
  }
}
