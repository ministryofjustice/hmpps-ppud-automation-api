package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.sentences

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.SentenceComparator
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CreatedSentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.exception.AutomationException
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.components.NavigationTreeViewComponent.Companion.url
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper

internal abstract class BaseSentencePage(
  protected val driver: WebDriver,
  protected val pageHelper: PageHelper,
  protected val navigationTreeViewComponent: NavigationTreeViewComponent,
  private val sentenceComparator: SentenceComparator,
) {

  protected abstract val pageDescription: String

  abstract fun selectCustodyType(custodyType: String)

  abstract fun createSentence(request: CreateOrUpdateSentenceRequest)

  abstract fun updateSentence(request: CreateOrUpdateSentenceRequest)

  abstract fun extractCreatedSentenceDetails(): CreatedSentence

  abstract fun extractSentenceDetails(
    offenceExtractor: (String) -> Offence,
  ): Sentence

  fun throwIfInvalid() {
    if (validationSummary?.text?.isNotBlank() == true) {
      throw AutomationException("Validation Failed.${System.lineSeparator()}${validationSummary?.text}")
    }
  }

  fun isMatching(request: CreateOrUpdateSentenceRequest): Boolean {
    // We use ::Offence, which will create an Offence with the offence page's URL as the index
    // offence (which doesn't make sense), because it isn't relevant to the sentence comparison
    val existing = extractSentenceDetails(::Offence)
    return sentenceComparator.areMatching(existing, request)
  }

  protected fun determineOffenceLink(): String {
    val sentenceId = pageHelper.extractId(pageDescription)
    return navigationTreeViewComponent
      .findOffenceNodeFor(sentenceId)
      .url
  }

  private val validationSummary: WebElement?
    get() = driver.findElements(By.id("cntDetails_ValidationSummary1")).firstOrNull()
}
