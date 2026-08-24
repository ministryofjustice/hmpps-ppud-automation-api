plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.6"
  kotlin("plugin.spring") version "2.4.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  testRuntimeClasspath {
    // MockServer 5.15.0 uses json-unit-core 2.36.0 for JSON body matching.
    // hmpps-subject-access-request-test-support pulls in json-unit-assertj:5.x which would
    // upgrade json-unit-core to 5.x, breaking MockServer's JSON matching.
    // Force json-unit-core back to the version MockServer was built against.
    resolutionStrategy.force("net.javacrumbs.json-unit:json-unit-core:2.36.0")
  }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-jackson2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.21.6") {
    because("Address CVE-2026-19032 & CVE-2026-68497")
  }
  implementation("tools.jackson.core:jackson-databind:3.1.6") {
    because("Address CVE-2026-19032 & CVE-2026-68497")
  }
  implementation("ch.qos.logback:logback-classic:1.6.3") {
    because("Address CVE-2026-19880")
  }
  implementation("ch.qos.logback:logback-core:1.6.3") {
    because("Address CVE-2026-19880")
  }

  implementation("org.seleniumhq.selenium:selenium-java:4.43.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.3.4")
  implementation("io.flipt:flipt-client-java:1.3.3")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.0")

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

  testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
  testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
  testImplementation("org.springframework.boot:spring-boot-starter-cache-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")

  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.jsonwebtoken:jjwt:0.13.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}

tasks.test {
  this.testLogging {
    // Add this so that we get more information on test failures for integration tests, particularly in the pipeline
    this.showStandardStreams = true
  }
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
