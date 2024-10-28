package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.domain.request

import io.swagger.v3.oas.annotations.Hidden
import net.minidev.json.annotate.JsonIgnore

data class UserSearchRequest(
  val fullName: String?,
  val userName: String?,
) {
  @get:JsonIgnore
  @get:Hidden
  internal val containsCriteria: Boolean
    get() {
      return !fullName.isNullOrBlank() ||
        !userName.isNullOrBlank()
    }
}
