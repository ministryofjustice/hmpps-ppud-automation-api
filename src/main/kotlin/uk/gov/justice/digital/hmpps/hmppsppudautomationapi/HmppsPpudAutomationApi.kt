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


//These are notes on the migration, to be added to the commit message. Reject if I
//forgot to remove them from here (the application should fail to compile anyway).
//
//SonarQube is already due to lose support at some point, as was announced in Slack
//a while back, so we've decided to use this migration to remove it altogether. With
//this, we also remove jacoco, which was generating the data for SonarQube. We haven't
//been doing separate checks on the code coverage data jacoco produces, so there's no
//point in keeping it. If we do need it in the future we can always re-add it.