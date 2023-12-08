package uk.gov.justice.digital.hmpps.hmppsppudautomationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import java.time.Duration

@Configuration
class CacheConfiguration {

  @Value("\${cache.timeToLiveSeconds}")
  var timeToLiveSeconds: Long = 20

  @Bean
  fun defaultRedisCacheConfiguration(): RedisCacheConfiguration {
    return RedisCacheConfiguration
      .defaultCacheConfig()
      .entryTtl(Duration.ofSeconds(timeToLiveSeconds))
  }
}
