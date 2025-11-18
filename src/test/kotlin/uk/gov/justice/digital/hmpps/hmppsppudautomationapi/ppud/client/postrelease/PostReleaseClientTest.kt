package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.client.postrelease

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.then
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease.PostReleaseConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.postrelease.postReleaseConfig
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender.SupportedCustodyType
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.postrelease.SupportedLicenceType.DCR
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.postrelease.SupportedLicenceType.DETERMINATE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.postrelease.SupportedLicenceType.IPP
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.postrelease.SupportedLicenceType.LIFE
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request.updatePostReleaseRequest
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.ppud.pages.PostReleasePage
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomEnum
import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

@ExtendWith(MockitoExtension::class)
internal class PostReleaseClientTest {

  @InjectMocks
  private lateinit var postReleaseClient: PostReleaseClient

  @Spy
  private val postReleaseConfig: PostReleaseConfig = postReleaseConfig()

  @Mock
  private lateinit var postReleasePage: PostReleasePage

  private val custodyTypesWithDeterminateLicenceType = enumValues<SupportedCustodyType>()
    .filter { it.licenceType === DETERMINATE }

  private val custodyTypesWithDcrLicenceType = enumValues<SupportedCustodyType>()
    .filter { it.licenceType === DCR }

  private val custodyTypesWithIppLicenceType = enumValues<SupportedCustodyType>()
    .filter { it.licenceType === IPP }

  private val custodyTypesWithLifeLicenceType = enumValues<SupportedCustodyType>()
    .filter { it.licenceType === LIFE }

  @Test
  fun `update post release for determinate sentence`() {
    // given
    val determinateCustodyType =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithIppLicenceType + custodyTypesWithLifeLicenceType + custodyTypesWithDcrLicenceType)
    val licenceType = postReleaseConfig.determinateLicenceType

    testUpdatePostRelease(determinateCustodyType, licenceType)
  }

  @Test
  fun `update post release for DCR sentence`() {
    // given
    val dcrCustodyType =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithDeterminateLicenceType + custodyTypesWithIppLicenceType + custodyTypesWithLifeLicenceType)
    val licenceType = postReleaseConfig.dcrLicenceType

    testUpdatePostRelease(dcrCustodyType, licenceType)
  }

  @Test
  fun `update post release for IPP sentence`() {
    // given
    val ippCustodyType =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithDeterminateLicenceType + custodyTypesWithLifeLicenceType)
    val licenceType = postReleaseConfig.ippLicenceType

    testUpdatePostRelease(ippCustodyType, licenceType)
  }

  @Test
  fun `update post release for life sentence`() {
    // given
    val lifeCustodyType =
      randomEnum<SupportedCustodyType>(exclude = custodyTypesWithDeterminateLicenceType + custodyTypesWithIppLicenceType)
    val licenceType = postReleaseConfig.lifeLicenceType

    testUpdatePostRelease(lifeCustodyType, licenceType)
  }

  private fun testUpdatePostRelease(
    custodyType: SupportedCustodyType,
    licenceType: String,
  ) {
    val releaseId = randomString()
    val updatePostReleaseRequest = updatePostReleaseRequest()

    // when
    postReleaseClient.updatePostRelease(releaseId, custodyType, updatePostReleaseRequest)

    // then
    val inOrder = inOrder(postReleasePage)
    then(postReleasePage).should(inOrder).navigateToPostReleaseFor(releaseId)
    then(postReleasePage).should(inOrder).updatePostRelease(updatePostReleaseRequest, licenceType)
    then(postReleasePage).should(inOrder).throwIfInvalid()
  }
}
