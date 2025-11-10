package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.willReturn
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.then
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.DETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.CustodyGroup.INDETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType.AUTOMATIC
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType.EDS
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType.EDS_NON_PAROLE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType.MANDATORY_MLP
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.LookupName.CustodyTypes
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.ReferenceDataPpudClient
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.DETERMINATE_CUSTODY_TYPES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.service.referencedata.ReferenceServiceImpl.Companion.INDETERMINATE_CUSTODY_TYPES_CACHE_NAME
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
class ReferenceServiceImplTest {

  // We use a spy in order to simply the cache refresh test, which would otherwise require more
  // complex mock set-ups to account for the custody type caches. Since the two more specific
  // custody methods are tested separately, it's OK to mock them in the refresh cache test
  @Spy
  @InjectMocks
  private lateinit var referenceService: ReferenceServiceImpl

  @Mock
  private lateinit var ppudClient: ReferenceDataPpudClient

  @Mock
  private lateinit var cacheManager: CacheManager

  // TODO MRD-2769 find out why log testing fails in CircleCI
//  private val logAppender: ListAppender<ILoggingEvent> = findLogAppender(ReferenceServiceImpl::class.java)

  @Test
  fun `refreshes the caches`() {
    runBlocking {
      // given
      val cacheNameToValuesMap = mutableMapOf<String, List<String>>()
      LookupName.entries.map { it.name }.associateWithTo(cacheNameToValuesMap) {
        listOf(
          randomString(),
          randomString(),
          randomString(),
        )
      }
      val cacheNameToCacheMap = mutableMapOf<String, Cache>()
      LookupName.entries.map { it.name }.associateWithTo(cacheNameToCacheMap) { mock<Cache>() }

      cacheNameToValuesMap.forEach {
        given(cacheManager.getCache(it.key)).willReturn(cacheNameToCacheMap.getValue(it.key))
        given(ppudClient.retrieveLookupValues(LookupName.valueOf(it.key))).willReturn(it.value)
      }

      val determinateValues = listOf(randomString(), randomString(), randomString())
      val determinateCache = mock<Cache>()
      given(cacheManager.getCache(DETERMINATE_CUSTODY_TYPES_CACHE_NAME)).willReturn(determinateCache)
      willReturn(determinateValues).given(referenceService).retrieveDeterminateCustodyTypes()
      cacheNameToValuesMap[DETERMINATE_CUSTODY_TYPES_CACHE_NAME] = determinateValues
      cacheNameToCacheMap[DETERMINATE_CUSTODY_TYPES_CACHE_NAME] = determinateCache

      val indeterminateValues = listOf(randomString(), randomString(), randomString())
      val indeterminateCache = mock<Cache>()
      given(cacheManager.getCache(INDETERMINATE_CUSTODY_TYPES_CACHE_NAME)).willReturn(indeterminateCache)
      willReturn(indeterminateValues).given(referenceService).retrieveIndeterminateCustodyTypes()
      cacheNameToValuesMap[INDETERMINATE_CUSTODY_TYPES_CACHE_NAME] = indeterminateValues
      cacheNameToCacheMap[INDETERMINATE_CUSTODY_TYPES_CACHE_NAME] = indeterminateCache

      // when
      referenceService.refreshCaches()

      // then
      cacheNameToCacheMap.forEach {
        then(it.value).should().put(SimpleKey.EMPTY, cacheNameToValuesMap.getValue(it.key))
      }
    }
  }

  @Test
  fun `returns all available supported determinate custody types, logging warnings for the missing ones`() {
    runBlocking {
      testCustodyTypeRetrievalByCustodyGroup(DETERMINATE, listOf(EDS, EDS_NON_PAROLE)) {
        referenceService.retrieveDeterminateCustodyTypes()
      }
    }
  }

  @Test
  fun `returns all available supported indeterminate custody types, logging warnings for the missing ones`() {
    runBlocking {
      testCustodyTypeRetrievalByCustodyGroup(INDETERMINATE, listOf(AUTOMATIC, MANDATORY_MLP)) {
        referenceService.retrieveIndeterminateCustodyTypes()
      }
    }
  }

  private suspend fun testCustodyTypeRetrievalByCustodyGroup(
    custodyGroup: CustodyGroup,
    missingCustodyTypes: List<SupportedCustodyType>,
    methodUnderTest: suspend () -> List<String>,
  ) {
    // given
    val expectedMissingCustodyTypeWarningMessages =
      missingCustodyTypes.map { "${custodyGroup.fullName} custody type not found in PPUD: ${it.fullName}" }

    val availableKnownCustodyTypes =
      SupportedCustodyType.entries.filterNot { missingCustodyTypes.contains(it) }
    val availableKnownCustodyTypesOfCustodyGroup = availableKnownCustodyTypes.filter { it.custodyGroup == custodyGroup }
    val expectedAvailableCustodyTypeNamesOfCustodyGroup = availableKnownCustodyTypesOfCustodyGroup.map { it.fullName }

    val availableKnownCustodyTypeNames = availableKnownCustodyTypes.map { it.fullName }
    val unsupportedCustodyTypeNames = listOf(randomString(), randomString())
    val availableCustodyTypeNames = availableKnownCustodyTypeNames + unsupportedCustodyTypeNames
    given(ppudClient.retrieveLookupValues(CustodyTypes)).willReturn(availableCustodyTypeNames)

    // when
    val actualAvailableCustodyTypeNamesOfCustodyGroup = methodUnderTest()

    // then
    assertThat(actualAvailableCustodyTypeNamesOfCustodyGroup).isEqualTo(expectedAvailableCustodyTypeNamesOfCustodyGroup)

    // TODO MRD-2769 find out why log testing fails in CircleCI
//    with(logAppender.list) {
//      this.forEach { assertThat(it.level).isEqualTo(Level.WARN) }
//      assertThat(this.map { it.message }).containsExactlyInAnyOrderElementsOf(
//        expectedMissingCustodyTypeWarningMessages,
//      )
//    }
  }
}
