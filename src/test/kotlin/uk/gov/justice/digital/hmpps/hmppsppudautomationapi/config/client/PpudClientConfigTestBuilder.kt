package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config.client

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun ppudClientConfig(
  url: String = randomString(),
) = PpudClientConfig(url)