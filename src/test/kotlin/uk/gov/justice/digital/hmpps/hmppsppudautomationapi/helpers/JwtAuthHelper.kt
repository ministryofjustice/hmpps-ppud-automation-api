package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.helpers

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthHelper {
  private val keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  @Bean
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  internal fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
    clientId: String = "community-api",
  ): String =
    mutableMapOf<String, Any>()
      .also { subject?.let { subject -> it["user_name"] = subject } }
      .also { it["client_id"] = "make-recall-decision-client" }
      .also { subject?.let { subject -> it["name"] = subject.lowercase() } }
      .also { roles?.let { roles -> it["authorities"] = roles } }
      .also { scope?.let { scope -> it["scope"] = scope } }
      .let {
        Jwts.builder()
          .setId(jwtId)
          .setSubject(subject)
          .addClaims(it.toMap())
          .setExpiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
          .signWith(SignatureAlgorithm.RS256, keyPair.private)
          .compact()
      }
}
