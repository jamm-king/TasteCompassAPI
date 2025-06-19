package com.tastecompass.redis.config

import com.tastecompass.redis.common.RedisProperties
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:redis.properties")
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    @Bean
    fun redisURI(): RedisURI {
        return try {
            RedisURI.Builder
                .redis(redisProperties.host, redisProperties.port.toInt())
                .withAuthentication(redisProperties.username, redisProperties.password)
                .build().also {
                    logger.info("Redis URI built: ${it.host}:${it.port}, SSL=${it.isSsl}")
                }
        } catch(e: Exception) {
            logger.error("Failed to build RedisURI: ${e.message}", e)
            throw IllegalStateException("Redis URI configuration failed", e)
        }
    }

    @Bean(destroyMethod = "shutdown")
    fun redisClient(uri: RedisURI): RedisClient {
        return try {
            RedisClient.create(uri).also {
                logger.info("RedisClient created successfully.")
            }
        } catch (e: Exception) {
            logger.error("Failed to create RedisClient: ${e.message}", e)
            throw IllegalStateException("Redis client creation failed", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RedisConfig::class.java)
    }
}
