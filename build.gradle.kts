plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.2.0"
  kotlin("plugin.spring") version "2.1.10"
  id("org.sonarqube") version "6.0.1.5171"
  id("jacoco")
}

jacoco.toolVersion = "0.8.11"
// OWASP fix https://mojdt.slack.com/archives/C69NWE339/p1734943189790819
ext["logback.version"] = "1.5.14"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.seleniumhq.selenium:selenium-java:4.29.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.1.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("commons-io:commons-io:2.18.0") // Address CVE-2021-29425

  // The dependencies below address the listed CVEs until the hmpps-gradle-spring-boot plug-in
  // brings in a newer version of spring-boot with the fixes (it's already bringing the latest
  // version of spring-boot, 3.5.0, but that doesn't have the fixes)
  implementation("org.springframework:spring-web:6.2.8") // Address CVE-2025-41234
  implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.42") // Address CVE-2025-49125 & CVE-2025-48988

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

  implementation("org.bouncycastle:bcprov-jdk18on:1.80") // Address CVE-2024-29857, CVE-2024-30172, CVE-2024-30171 present in 1.76

  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.jsonwebtoken:jjwt:0.12.6")
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

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}
