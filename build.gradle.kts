plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.7"
  kotlin("plugin.spring") version "2.3.21"
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
  // CVE version overrides - the hmpps-gradle-spring-boot plugin uses resolution rules that
  // override BOM constraints, so we must use eachDependency to force specific versions.
  // These can be removed once the plugin is upgraded to 11.0.x
  all {
    resolutionStrategy.eachDependency {
      if (requested.group == "io.netty" && requested.name.startsWith("netty-") && !requested.name.startsWith("netty-tcnative")) {
        useVersion("4.2.16.Final")
        because("Address CVE-2026-44891, CVE-2026-55831, CVE-2026-55833 and other Netty CVEs")
      }
      if (requested.group == "org.apache.logging.log4j") {
        useVersion("2.26.1")
        because("Address CVE-2026-49844")
      }
      if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-databind") {
        useVersion("2.22.1")
        because("Address CVE-2026-54515 and CVE-2026-59889")
      }
      if (requested.group == "tools.jackson.core" && requested.name == "jackson-databind") {
        useVersion("3.1.5")
        because("Address CVE-2026-59889")
      }
    }
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

  implementation("org.seleniumhq.selenium:selenium-java:4.43.0")
  implementation("io.github.bonigarcia:webdrivermanager:6.3.4")
  implementation("io.flipt:flipt-client-java:1.3.1")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  // OAuth dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.security:spring-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

  // OpenAPI dependencies
  // Not sure if we're affected, but release notes on 10.2.1 version of hmpps-gradle-spring-boot
  // reported some issues encountered and recommended pinning swagger-ui to 5.32.2 and not updating
  // the springdoc dependency for now
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  constraints {
    implementation("org.webjars:swagger-ui:5.32.2")
  }

  // hmpps-spring-boot plugin explicitly forcing the tomcat-embed-core version, so we can't override using constraints
  implementation("org.apache.tomcat.embed:tomcat-embed-websocket") {
    version {
      strictly("11.0.24")
    }
    because("Address CVE-2026-59084 - can be removed once uk.gov.justice.hmpps.gradle-spring-boot to 11.0.x ")
  }
  // hmpps-spring-boot plugin explicitly forcing the tomcat-embed-core version, so we can't override using constraints
  implementation("org.apache.tomcat.embed:tomcat-embed-core") {
    version {
      strictly("11.0.24")
    }
    because("Address CVE-2026-59084 - can be removed once uk.gov.justice.hmpps.gradle-spring-boot to 11.0.x ")
  }

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
