plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.2.0"
  kotlin("plugin.spring") version "2.2.21"
  id("org.sonarqube") version "7.2.1.6560"
  id("jacoco")
}

jacoco.toolVersion = "0.8.11"

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
  implementation("org.seleniumhq.selenium:selenium-java:4.38.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.3.3")
  implementation("io.flipt:flipt-client-java:1.2.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

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
