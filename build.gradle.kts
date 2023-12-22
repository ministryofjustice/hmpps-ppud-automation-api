plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.11.0"
  kotlin("plugin.spring") version "1.9.22"
  id("org.sonarqube") version "4.4.1.3373"
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
  implementation("org.seleniumhq.selenium:selenium-java:4.16.1")
  implementation("org.seleniumhq.selenium:selenium-http-jdk-client:4.13.0")
  implementation("io.github.bonigarcia:webdrivermanager:5.6.2")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("commons-io:commons-io:2.15.1") // Address CVE-2021-29425
  implementation("org.seleniumhq.selenium:selenium-api:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-chromium-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-devtools-v85:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-edge-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-firefox-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-http:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-ie-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-json:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-manager:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-remote-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-safari-driver:4.16.1") // Address CVE-2023-5590
  implementation("org.seleniumhq.selenium:selenium-support:4.16.1") // Address CVE-2023-5590

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.jsonwebtoken:jjwt:0.12.3")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(20))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "20"
    }
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}
