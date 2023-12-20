package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class YoungOffenderCalculatorTest {

  @Mock
  private lateinit var currentDate: CurrentDate

  private val assumedCurrentDate = LocalDate.parse("2021-06-15")

  private lateinit var youngOffenderCalculator: YoungOffenderCalculator

  @BeforeEach
  fun beforeEach() {
    given(currentDate.now()).willReturn(assumedCurrentDate)
    youngOffenderCalculator = YoungOffenderCalculator(currentDate)
  }

  @ParameterizedTest
  @ValueSource(
    strings = ["2010-01-01", "2005-06-01", "2000-06-17", "2000-06-16"],
  )
  fun `given offender is younger than 21 when isYoungOffenderIsCalled then they are a young offender`(dateOfBirth: String) {
    val result = youngOffenderCalculator.isYoungOffender(LocalDate.parse(dateOfBirth))
    assertEquals(true, result)
  }

  @ParameterizedTest
  @ValueSource(
    strings = ["1970-01-01", "1980-06-01", "2000-06-14", "2000-06-15"],
  )
  fun `given offender is 21 years or older when isYoungOffenderIsCalled then they are not a young offender`(dateOfBirth: String) {
    val result = youngOffenderCalculator.isYoungOffender(LocalDate.parse(dateOfBirth))
    assertEquals(false, result)
  }
}
