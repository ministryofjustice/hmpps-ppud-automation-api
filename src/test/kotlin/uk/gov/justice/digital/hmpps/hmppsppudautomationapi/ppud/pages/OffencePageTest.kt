package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Offence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.UpdateOffenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.helpers.PageHelper.Companion.getValue
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class OffencePageTest {

  private lateinit var offencePage: OffencePage

  @Mock
  private lateinit var saveButton: WebElement

  @Mock
  private lateinit var dateOfIndexOffenceInput: WebElement

  @Mock
  private lateinit var indexOffenceInput: WebElement

  @Mock
  private lateinit var indexOffenceCommentInput: WebElement

  @Mock
  private lateinit var indexOffenceDropdown: WebElement

  @Mock
  private lateinit var driver: WebDriver

  @Mock
  private lateinit var pageHelper: PageHelper

  // As defined in ComponentConfiguration. Mockito doesn't mock static methods well, so
  // we don't mock LocalDate.parse nor the DateTimeFormatter
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  @BeforeEach
  fun beforeEach() {
    offencePage = OffencePage(driver, pageHelper, dateTimeFormatter)

    ReflectionTestUtils.setField(offencePage, "saveButton", saveButton)
    ReflectionTestUtils.setField(offencePage, "dateOfIndexOffenceInput", dateOfIndexOffenceInput)
    ReflectionTestUtils.setField(offencePage, "indexOffenceInput", indexOffenceInput)
    ReflectionTestUtils.setField(offencePage, "indexOffenceCommentInput", indexOffenceCommentInput)
    ReflectionTestUtils.setField(offencePage, "indexOffenceDropdown", indexOffenceDropdown)
  }

  @Test
  fun `update offence`() {
    runBlocking {
      val request: UpdateOffenceRequest = UpdateOffenceRequest(
        indexOffence = "offence",
        indexOffenceComment = "comment",
        dateOfIndexOffence = LocalDate.now(),
      )

      val result = offencePage.updateOffence(request)

      verify(pageHelper).enterText(indexOffenceInput, request.indexOffence)
      verify(pageHelper).enterTextIfNotBlank(indexOffenceCommentInput, request.indexOffenceComment)
      verify(pageHelper).enterDate(dateOfIndexOffenceInput, request.dateOfIndexOffence)
      verify(pageHelper).selectDropdownOptionIfNotBlank(indexOffenceDropdown, request.indexOffence, "index offence")

      verify(saveButton).click()
    }
  }

  @Test
  fun `extract offence`() {
    runBlocking {
      val dateOfIndexOffence = randomDate()
      given(dateOfIndexOffenceInput.getValue()).willReturn(dateTimeFormatter.format(dateOfIndexOffence))
      given(indexOffenceInput.getValue()).willReturn("offence")
      given(indexOffenceCommentInput.getValue()).willReturn("offenceComment")

      val offence = offencePage.extractOffenceDetails()

      // TODO Mock LocalDate's parse function (currently not functioning) and check offence contents

      assertThat(offence).usingRecursiveComparison().isEqualTo(
        Offence(
          dateOfIndexOffence = dateOfIndexOffence,
          indexOffence = "offence",
          offenceComment = "offenceComment",
        ),
      )

      verify(dateOfIndexOffenceInput).getValue()
      verify(indexOffenceInput).getValue()
      verify(indexOffenceCommentInput).getValue()
    }
  }
}
