package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.Sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SentenceLength
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.custodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.randomDeterminateCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.randomIndeterminateCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.sentence
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.createOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomDate
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.random.Random

internal class SentenceComparatorTest {

  private lateinit var comparator: SentenceComparator

  companion object {

    /**
     *  These are the key fields on which we base a match for determinate sentences.
     */
    @JvmStatic
    fun keyDeterminateFieldsSource(): Stream<String> = Stream.of(
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

    /**
     *  These are the key fields on which we base a match for indeterminate sentences.
     */
    @JvmStatic
    fun keyIndeterminateFieldsSource(): Stream<String> = Stream.of(
      "custodyType",
      "dateOfSentence",
      "sentencingCourt",
    )
  }

  @BeforeEach
  fun beforeEach() {
    comparator = SentenceComparator()
  }

  @Test
  fun `matching key determinate values result in matching objects`() {
    val existing = sentence(
      id = "",
      custodyType = randomDeterminateCustodyType(),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = randomDate(),
      sentenceExpiryDate = randomDate(),
      sentenceLength = SentenceLength(Random.nextInt(), Random.nextInt(), Random.nextInt()),
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = randomString("sentencedUnder"),
    )
    val request = createOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      mappaLevel = existing.mappaLevel.orEmpty(),
      licenceExpiryDate = existing.licenceExpiryDate,
      sentenceExpiryDate = existing.sentenceExpiryDate,
      sentenceLength = SentenceLength(
        existing.sentenceLength!!.partYears,
        existing.sentenceLength!!.partMonths,
        existing.sentenceLength!!.partDays,
      ),
      sentencingCourt = existing.sentencingCourt,
      sentencedUnder = existing.sentencedUnder!!,
    )
    val result = comparator.areMatching(existing, request)
    assertTrue(result)
  }

  @Test
  fun `matching key indeterminate values result in matching objects`() {
    val existing = sentence(
      id = "",
      custodyType = randomIndeterminateCustodyType(),
      dateOfSentence = randomDate(),
      sentencingCourt = randomString("sentencingCourt"),
    )
    val request = createOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      sentencingCourt = existing.sentencingCourt,
    )
    val result = comparator.areMatching(existing, request)
    assertTrue(result)
  }

  @Test
  fun `considers null key determinate values (where allowed) as equal`() {
    val existing = Sentence(
      id = "",
      custodyType = randomDeterminateCustodyType(),
      dateOfSentence = randomDate(),
      mappaLevel = null,
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = null,
    )
    val request = createOrUpdateSentenceRequest(
      custodyType = existing.custodyType,
      dateOfSentence = existing.dateOfSentence,
      mappaLevel = null,
      licenceExpiryDate = null,
      sentenceExpiryDate = null,
      sentenceLength = null,
      sentencingCourt = existing.sentencingCourt,
      sentencedUnder = null,
    )
    val result = comparator.areMatching(existing, request)
    assertTrue(result)
  }

  // All key comparison values for indeterminate sentences are non-nullable, so we don't need a null comparison test

  @ParameterizedTest
  @MethodSource("keyDeterminateFieldsSource")
  fun `differing key determinate values result in non-matching objects`(
    keyDeterminateFieldToBeDifferent: String,
  ) {
    val existing = sentence(
      custodyType = randomDeterminateCustodyType(),
      dateOfSentence = randomDate(),
      mappaLevel = randomString("mappaLevel"),
      licenceExpiryDate = randomDate(),
      sentenceExpiryDate = randomDate(),
      sentenceLength = SentenceLength(Random.nextInt(), Random.nextInt(), Random.nextInt()),
      sentencingCourt = randomString("sentencingCourt"),
      sentencedUnder = randomString("sentencedUnder"),
    )
    val request = createOrUpdateSentenceRequest(
      custodyType = differsOrTheSameCustodyType(keyDeterminateFieldToBeDifferent, existing.custodyType),
      dateOfSentence = differsOrTheSame(keyDeterminateFieldToBeDifferent, "dateOfSentence", existing.dateOfSentence),
      mappaLevel = differsOrTheSame(keyDeterminateFieldToBeDifferent, "mappaLevel", existing.mappaLevel.orEmpty()),
      licenceExpiryDate = differsOrTheSame(
        keyDeterminateFieldToBeDifferent,
        "licenceExpiryDate",
        existing.licenceExpiryDate!!,
      ),
      sentenceExpiryDate = differsOrTheSame(
        keyDeterminateFieldToBeDifferent,
        "sentenceExpiryDate",
        existing.sentenceExpiryDate!!,
      ),
      sentenceLength = SentenceLength(
        partYears = differsOrTheSame(
          keyDeterminateFieldToBeDifferent,
          "sentenceLength.partYears",
          existing.sentenceLength!!.partYears,
        ),
        partMonths = differsOrTheSame(
          keyDeterminateFieldToBeDifferent,
          "sentenceLength.partMonths",
          existing.sentenceLength!!.partMonths,
        ),
        partDays = differsOrTheSame(
          keyDeterminateFieldToBeDifferent,
          "sentenceLength.partDays",
          existing.sentenceLength!!.partDays,
        ),
      ),
      sentencingCourt = differsOrTheSame(keyDeterminateFieldToBeDifferent, "sentencingCourt", existing.sentencingCourt),
      sentencedUnder = differsOrTheSame(keyDeterminateFieldToBeDifferent, "sentencedUnder", existing.sentencedUnder!!),
      // non-key fields - made the same to ensure they don't influence the comparison result
      releaseDate = existing.releaseDate,
      espCustodialPeriod = existing.espCustodialPeriod,
      espExtendedPeriod = existing.espExtendedPeriod,
    )
    val result = comparator.areMatching(existing, request)
    assertFalse(result)
  }

  @ParameterizedTest
  @MethodSource("keyIndeterminateFieldsSource")
  fun `differing key indeterminate values result in non-matching objects`(
    keyIndeterminateFieldToBeDifferent: String,
  ) {
    val existing = sentence(custodyType = randomIndeterminateCustodyType())
    val request = createOrUpdateSentenceRequest(
      custodyType = differsOrTheSameCustodyType(keyIndeterminateFieldToBeDifferent, existing.custodyType),
      dateOfSentence = differsOrTheSame(keyIndeterminateFieldToBeDifferent, "dateOfSentence", existing.dateOfSentence),
      sentencingCourt = differsOrTheSame(
        keyIndeterminateFieldToBeDifferent,
        "sentencingCourt",
        existing.sentencingCourt,
      ),
      // non-key fields - made the same to ensure they don't influence the comparison result
      licenceExpiryDate = existing.licenceExpiryDate,
      mappaLevel = existing.mappaLevel,
      releaseDate = existing.releaseDate,
      sentenceLength = existing.sentenceLength,
      espCustodialPeriod = existing.espCustodialPeriod,
      espExtendedPeriod = existing.espExtendedPeriod,
      sentenceExpiryDate = existing.sentenceExpiryDate,
      sentencedUnder = existing.sentencedUnder,
    )
    val result = comparator.areMatching(existing, request)
    assertFalse(result)
  }

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: String): String = if (keyFieldToBeDifferent == fieldName) "differs" else value

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: LocalDate): LocalDate = if (keyFieldToBeDifferent == fieldName) value.plusDays(1) else value

  private fun differsOrTheSame(keyFieldToBeDifferent: String, fieldName: String, value: Int): Int = if (keyFieldToBeDifferent == fieldName) value - 1 else value

  private fun differsOrTheSameCustodyType(keyFieldToBeDifferent: String, value: String): String = if (keyFieldToBeDifferent == "custodyType") custodyType(value) else value
}
