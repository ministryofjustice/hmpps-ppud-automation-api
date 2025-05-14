package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.testdata

import kotlin.random.Random

fun randomBoolean(): Boolean {
  return Random.Default.nextBoolean()
}

fun randomInt(): Int {
  return Random.Default.nextInt()
}