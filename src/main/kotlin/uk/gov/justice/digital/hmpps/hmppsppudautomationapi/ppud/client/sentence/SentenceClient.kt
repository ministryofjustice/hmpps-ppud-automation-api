package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.sentence

import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client.PpudClientConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.offence.OffenceClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.OffenderPage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.BaseSentencePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences.SentencePageFactory

@Service
internal class SentenceClient {

  @Autowired
  private lateinit var ppudClientConfig: PpudClientConfig

  @Autowired
  private lateinit var offenderPage: OffenderPage

  @Autowired
  private lateinit var navigationTreeViewComponent: NavigationTreeViewComponent

  @Autowired
  private lateinit var driver: WebDriver

  @Autowired
  private lateinit var sentencePageFactory: SentencePageFactory

  @Autowired
  private lateinit var offenceClient: OffenceClient

  fun createSentence(offenderId: String, request: CreateOrUpdateSentenceRequest): CreatedSentence {
    offenderPage.viewOffenderWithId(offenderId)
    val matched = navigateToMatchingSentence(request)

    if (!matched) {
      navigationTreeViewComponent.navigateToNewSentence()
      val newSentencePage = sentencePageFactory.sentencePage()
      // custody type needs to be selected first, as it changes the page layout
      newSentencePage.selectCustodyType(request.custodyType)
      // the page factory needs to be called again, as the New Sentence button in
      // PPUD always takes us first to the indeterminate page, but switches to the
      // determinate one if such a custody type is selected
      val sentencePage = sentencePageFactory.sentencePage()
      sentencePage.createSentence(request)
      sentencePage.throwIfInvalid()
    }

    return sentencePageFactory.sentencePage().extractCreatedSentenceDetails()
  }

  fun updateSentence(offenderId: String, sentenceId: String, request: CreateOrUpdateSentenceRequest) {
    val sentencePage = getSentencePage(offenderId, sentenceId)
    sentencePage.updateSentence(request)
    sentencePage.throwIfInvalid()
  }

  fun getSentence(offenderId: String, sentenceId: String): Sentence {
    val sentencePage = getSentencePage(offenderId, sentenceId)
    return sentencePage.extractSentenceDetails(offenceClient::getOffence)
  }

  private fun navigateToMatchingSentence(request: CreateOrUpdateSentenceRequest): Boolean {
    val sentenceLinks = navigationTreeViewComponent.extractSentenceLinks(request.dateOfSentence, request.custodyType)
    return sentenceLinks.any {
      driver.navigate().to("${ppudClientConfig.url}$it")
      sentencePageFactory.sentencePage().isMatching(request)
    }
  }

  private fun getSentencePage(
    offenderId: String,
    sentenceId: String,
  ): BaseSentencePage {
    offenderPage.viewOffenderWithId(offenderId)
    navigationTreeViewComponent.navigateToSentenceFor(sentenceId)
    return sentencePageFactory.sentencePage()
  }
}
