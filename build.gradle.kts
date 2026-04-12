plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.6.0"
  kotlin("plugin.spring") version "2.3.10"
}

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
  implementation("org.seleniumhq.selenium:selenium-java:4.41.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.3.3")
  implementation("io.flipt:flipt-client-java:1.2.1")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // OpenAPI dependencies
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  // Temporary fix to address CVE-2026-0540, CVE-2025-15599, should be removable once
  // springdoc-openapi-starter-webmvc-ui above pulls later version of swagger-ui
  implementation("org.webjars:swagger-ui:5.32.1")

  // Temporary fix to address CVE-2025-68161 until we upgrade to spring-boot 4 or a 3.5.x with the fix is released
  implementation("org.apache.logging.log4j:log4j-api:2.25.3")

  // The following pinned netty dependencies are to address CVE-2026-33871 and CVE-2026-33870. Spring Boot 3.5.13
  // addresses this, but it is currently only a few days old, so will wait for a bit more before releasing a new HMPPS
  // plug-in version with it and pulling it here
  implementation("io.netty:netty-buffer:4.2.12.Final")
  implementation("io.netty:netty-codec:4.2.12.Final")
  implementation("io.netty:netty-codec-dns:4.2.12.Final")
  implementation("io.netty:netty-codec-http:4.2.12.Final")
  implementation("io.netty:netty-codec-http2:4.2.12.Final")
  implementation("io.netty:netty-codec-socks:4.2.12.Final")
  implementation("io.netty:netty-common:4.2.12.Final")
  implementation("io.netty:netty-handler:4.2.12.Final")
  implementation("io.netty:netty-handler-proxy:4.2.12.Final")
  implementation("io.netty:netty-resolver:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-classes-macos:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-native-macos:4.2.12.Final")
  implementation("io.netty:netty-resolver-dns-native-macos:4.2.12.Final")
  implementation("io.netty:netty-transport:4.2.12.Final")
  implementation("io.netty:netty-transport-classes-epoll:4.2.12.Final")
  implementation("io.netty:netty-transport-native-epoll:4.2.12.Final")
  implementation("io.netty:netty-transport-native-unix-common:4.2.12.Final")

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
