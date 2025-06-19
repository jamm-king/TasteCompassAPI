package com.tastecompass.redis.client

import io.lettuce.core.RedisClient
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RedisClientWrapperImpl(
    private val redisClient: RedisClient
): RedisClientWrapper {

    private val connection = redisClient.connect()
    private val syncCommands = connection.sync()

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down RedisClientWrapper...")
        connection.close()
    }

    override fun get(key: String): String? = try {
        syncCommands.get(key)
    } catch(e: Exception) {
        logger.error("Redis GET failed for key=$key", e)
        null
    }

    override fun set(key: String, value: String, ttlSeconds: Long?) {
        try {
            if (ttlSeconds != null) {
                syncCommands.setex(key, ttlSeconds, value)
            } else {
                syncCommands.set(key, value)
            }
        } catch (e: Exception) {
            logger.error("Redis SET failed for key=$key", e)
        }
    }

    override fun delete(key: String) {
        try {
            syncCommands.del(key)
        } catch (e: Exception) {
            logger.error("Redis DEL failed for key=$key", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}