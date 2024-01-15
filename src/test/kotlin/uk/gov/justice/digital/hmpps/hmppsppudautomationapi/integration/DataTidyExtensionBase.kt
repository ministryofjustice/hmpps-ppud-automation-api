package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.integration

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers.JwtAuthHelper
import java.util.UUID

internal abstract class DataTidyExtensionBase : AfterAllCallback {

  protected lateinit var webTestClient: WebTestClient

  private lateinit var jwtAuthHelper: JwtAuthHelper

  protected fun HttpHeaders.dataTidyAuthToken(
    roles: List<String> = listOf("ROLE_PPUD_AUTOMATION__TESTS__READWRITE"),
    subject: String? = "SOME_USER",
  ) {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "$subject",
        roles = roles,
      ),
    )
  }

  override fun afterAll(context: ExtensionContext) {
    val applicationContext = SpringExtension.getApplicationContext(context)
    webTestClient = applicationContext.getBean(WebTestClient::class.java) as WebTestClient
    jwtAuthHelper = applicationContext.getBean(JwtAuthHelper::class.java) as JwtAuthHelper
    afterAllTidy()
  }

  protected abstract fun afterAllTidy()

  @Suppress("SameParameterValue")
  protected fun deleteTestOffenders(familyNamePrefix: String, testRunId: UUID) {
    webTestClient
      .delete()
      .uri(
        "/offender?" +
          "familyNamePrefix=$familyNamePrefix" +
          "&testRunId=$testRunId",
      )
      .headers { it.dataTidyAuthToken() }
      .exchange()
      .expectStatus()
      .isOk
  }
}
