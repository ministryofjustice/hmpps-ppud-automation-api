package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      sessionManagement { SessionCreationPolicy.STATELESS }
      headers { frameOptions { sameOrigin = true } }
      csrf { disable() }
      authorizeHttpRequests {
        // TODO: Remove permitall for functional endpoints
        listOf(
          "/offender/**",
          "/reference/**",
          "/webjars/**",
          "/favicon.ico",
          "/health/**",
          "/info",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/h2-console/**",
        ).forEach { authorize(it, permitAll) }
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
    }

    return http.build()
  }
}
