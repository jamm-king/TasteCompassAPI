package com.tastecompass.redis.client

interface RedisClientWrapper {
    fun get(key: String): String?
    fun set(key: String, value: String, ttlSeconds: Long? = null)
    fun delete(key: String)
}