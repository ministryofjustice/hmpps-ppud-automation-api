package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import net.minidev.json.JSONArray
import java.util.function.Consumer

class ValueDoesNotContainConsumer(private val element: String) : Consumer<JSONArray> {

  override fun accept(t: JSONArray) {
    if (t.contains(element)) {
      throw AssertionError("Array contains unwanted element '$element'")
    }
  }
}

fun doesNotContain(element: String): Consumer<JSONArray> {
  return ValueDoesNotContainConsumer(element)
}
