package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offender

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedOffender
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOffenderRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.NewOffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.SearchPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.SentencePageFactory
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.offender.validation.OffenderValidator

@Service
class OffenderClient {
  @Value("\${ppud.url}")
  private lateinit var ppudUrl: String

  @Autowired
  private lateinit var offenderValidator: OffenderValidator

  @Autowired
  private lateinit var searchPage: SearchPage

  @Autowired
  private lateinit var newOffenderPage: NewOffenderPage

  @Autowired
  private lateinit var offenderPage: OffenderPage

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var sentencePageFactory: SentencePageFactory

  fun createOffender(request: CreateOffenderRequest): CreatedOffender {
    offenderValidator.validateOffenderCreationRequest(request)

    searchPage.navigateToNewOffender()
    newOffenderPage.verifyOn()
    newOffenderPage.createOffender(request)
    newOffenderPage.throwIfInvalid()
    offenderPage.verifyOn()
    offenderPage.updateAdditionalAddresses(request.additionalAddresses)
    offenderPage.throwIfInvalid()
    return offenderPage.extractCreatedOffenderDetails(::extractCreatedSentence)
  }

  private fun extractCreatedSentence(sentenceLink: String): CreatedSentence {
    driver.navigate().to("$ppudUrl$sentenceLink")
    val sentencePage = sentencePageFactory.sentencePage()
    return sentencePage.extractCreatedSentenceDetails()
  }
}
