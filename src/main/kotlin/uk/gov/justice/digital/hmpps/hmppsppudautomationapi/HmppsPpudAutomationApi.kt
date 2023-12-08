package uk.gov.justice.digital.hmpps.hmppsppudautomationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class HmppsPpudAutomationApi

fun main(args: Array<String>) {
  runApplication<HmppsPpudAutomationApi>(*args)
}
