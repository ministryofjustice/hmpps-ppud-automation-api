package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import java.util.function.Consumer

class ValueConsumer<T> : Consumer<T> {

  var value: T? = null
  override fun accept(t: T) {
    value = t
  }
}
