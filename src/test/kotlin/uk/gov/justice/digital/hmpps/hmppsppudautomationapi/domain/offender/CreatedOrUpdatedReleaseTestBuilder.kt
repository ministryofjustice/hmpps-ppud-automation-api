package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.offender

import uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata.randomString

internal fun createdOrUpdatedRelease(id: String = randomString()) = CreatedOrUpdatedRelease(id)