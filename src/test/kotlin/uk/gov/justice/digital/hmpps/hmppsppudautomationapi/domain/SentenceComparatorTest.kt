package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.CreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.random.Random

internal class SentenceComparatorTest {

  private lateinit var comparator: SentenceComparator

  companion object {

    /**
     *  These are the key fields on which we base a match.
     */
    @JvmStatic
    fun keyFieldsSource(): Stream<String> = Stream.of(
      "custodyType",
      "dateOfSentence",
      "licenceExpiryDate",
      "mappaLevel",
      "sentenceExpiryDate",
      "sentenceLength.partYears",
      "sentenceLength.partMonths",
      "sentenceLength.partDays",
      "sentencingCourt",
      "sentencedUnder",
    )
  }

  @BeforeEach
  fun beforeEach() {
    comparator = SentenceComparator()
  }

  @Test
  fun `given all key values are populated with matching values when areMatching is called then true is returned`() {
    val existing = Sentence(
      id = "",
      custodyType = randomString("custodyType"),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = randomDate(),
      sentenceExpiryDate = randomDate(),
      sentenceLength = SentenceLength(Random.nextInt(), Random.nextInt(), Random.nextInt()),
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = randomString("sentencedUnder"),
    )
    val request = CreateOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      mappaLevel = existing.mappaLevel,
      licenceExpiryDate = existing.licenceExpiryDate,
      sentenceExpiryDate = existing.sentenceExpiryDate,
      sentenceLength = SentenceLength(
        existing.sentenceLength!!.partYears,
        existing.sentenceLength!!.partMonths,
        existing.sentenceLength!!.partDays,
      ),
      sentencingCourt = existing.sentencingCourt,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      releaseDate = null,
      sentencedUnder = existing.sentencedUnder!!,
    )
    val result = comparator.areMatching(existing, request)
    assertTrue(result)
  }

  @Test
  fun `given nullable values are null and other key values are populated with matching values when areMatching is called then true is returned`() {
    val existing = Sentence(
      id = "",
      custodyType = randomString("custodyType"),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = randomString("sentencedUnder"),
    )
    val request = CreateOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      mappaLevel = existing.mappaLevel,
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = existing.sentencingCourt,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      releaseDate = null,
      sentencedUnder = existing.sentencedUnder!!,
    )
    val result = comparator.areMatching(existing, request)
    assertTrue(result)
  }

  @Test
  fun `given all fields but sentencedUnder are populated with matching values when areMatching is called then false is returned`() {
    // We have this test because Sentence allows a null sentencedUnder (indeterminate sentences have a null value here)
    // but CreateOrUpdateSentenceRequest doesn't (only determinate sentences are ever created or updated, and they
    // always have a value for sentencedUnder). We want to make sure null vs !null is covered.
    val existing = Sentence(
      id = "",
      custodyType = randomString("custodyType"),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = null,
    )
    val request = CreateOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      mappaLevel = existing.mappaLevel,
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = existing.sentencingCourt,
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      releaseDate = null,
      sentencedUnder = randomString("sentencedUnder"),
    )
    val result = comparator.areMatching(existing, request)
    assertFalse(result)
  }

  @ParameterizedTest
  @MethodSource("keyFieldsSource")
  fun `given any key value differs when areMatching is called then false is returned`(
    keyFieldToBeDifferent: String,
  ) {
    val existing = Sentence(
      id = "",
      custodyType = randomString("custodyType"),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = randomDate(),
      sentenceExpiryDate = randomDate(),
      sentenceLength = SentenceLength(Random.nextInt(), Random.nextInt(), Random.nextInt()),
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = randomString("sentencedUnder"),
    )
    val request = CreateOrUpdateSentenceRequest(
      custodyType = differsOrTheSame(keyFieldToBeDifferent, "custodyType", existing.custodyType),
      dateOfSentence = differsOrTheSame(keyFieldToBeDifferent, "dateOfSentence", existing.dateOfSentence),
      mappaLevel = differsOrTheSame(keyFieldToBeDifferent, "mappaLevel", existing.mappaLevel),
      licenceExpiryDate = differsOrTheSame(keyFieldToBeDifferent, "licenceExpiryDate", existing.licenceExpiryDate!!),
      sentenceExpiryDate = differsOrTheSame(keyFieldToBeDifferent, "sentenceExpiryDate", existing.sentenceExpiryDate!!),
      sentenceLength = SentenceLength(
        partYears = differsOrTheSame(
          keyFieldToBeDifferent,
          "sentenceLength.partYears",
          existing.sentenceLength!!.partYears,
        ),
        partMonths = differsOrTheSame(
          keyFieldToBeDifferent,
          "sentenceLength.partMonths",
          existing.sentenceLength!!.partMonths,
        ),
        partDays = differsOrTheSame(
          keyFieldToBeDifferent,
          "sentenceLength.partDays",
          existing.sentenceLength!!.partDays,
        ),
      ),
      sentencingCourt = differsOrTheSame(keyFieldToBeDifferent, "sentencingCourt", existing.sentencingCourt),
      espCustodialPeriod = null,
      espExtendedPeriod = null,
      releaseDate = null,
      sentencedUnder = differsOrTheSame(keyFieldToBeDifferent, "sentencedUnder", existing.sentencedUnder!!),
    )
    val result = comparator.areMatching(existing, request)
    assertFalse(result)
  }

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: String): String = if (keyFieldToBeDifferent == fieldName) "differs" else value

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: LocalDate): LocalDate = if (keyFieldToBeDifferent == fieldName) value.plusDays(1) else value

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: Int): Int = if (keyFieldToBeDifferent == fieldName) value - 1 else value
}
