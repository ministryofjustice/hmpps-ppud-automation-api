package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import java.util.function.Consumer

class ValueIsNullConsumer<T> : Consumer<T> {

  override fun accept(t: T) {
    if (t != null) {
      throw AssertionError("Value is not null")
    }
  }
}

fun isNull(): Consumer<String> {
  return ValueIsNullConsumer()
}
