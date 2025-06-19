package com.tastecompass.redis.client

import com.tastecompass.redis.config.RedisConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RedisConfig::class, RedisClientWrapperImpl::class])
class RedisClientWrapperImplTest {
    @Autowired
    private lateinit var redisClientWrapper: RedisClientWrapper

    private val testKey = "test:key"
    private val testValue = "hello"

    @AfterEach
    fun cleanup() {
        redisClientWrapper.delete(testKey)
    }

    @Test
    fun `should set and get a value`() {
        redisClientWrapper.set(testKey, testValue)
        val result = redisClientWrapper.get(testKey)
        assertEquals(testValue, result)
    }

    @Test
    fun `should expire value after ttl`() {
        redisClientWrapper.set(testKey, testValue, ttlSeconds = 1)
        Thread.sleep(1500)
        val result = redisClientWrapper.get(testKey)
        assertNull(result)
    }
}