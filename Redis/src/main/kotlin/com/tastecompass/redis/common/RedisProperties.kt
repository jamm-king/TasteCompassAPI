package com.tastecompass.redis.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="redis")
class RedisProperties {
    lateinit var host: String
    lateinit var port: String
    lateinit var username: String
    lateinit var password: String
}