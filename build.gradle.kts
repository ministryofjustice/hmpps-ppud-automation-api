plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.5"
  kotlin("plugin.spring") version "1.9.23"
  id("org.sonarqube") version "5.0.0.4638"
  id("jacoco")
}

jacoco.toolVersion = "0.8.11"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
  implementation("io.github.bonigarcia:webdrivermanager:5.8.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("commons-io:commons-io:2.16.1") // Address CVE-2021-29425

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  implementation("org.bouncycastle:bcprov-jdk18on:1.78") // Address CVE-2024-29857, CVE-2024-30172, CVE-2024-30171 present in 1.76

  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.jsonwebtoken:jjwt:0.12.5")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
  this.testLogging {
    // Add this so that we get more information on test failures for integration tests, particularly in the pipeline
    this.showStandardStreams = true
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}
