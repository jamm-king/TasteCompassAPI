package com.config

import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.client.ConnectConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.logging.Logger

@Configuration
open class MilvusConfig {
    @Value("\${spring.db.milvus.endpoint}")
    lateinit var ENDPOINT_URI: String
    @Value("\${spring.db.milvus.token}")
    lateinit var BEAR_TOKEN: String

    private val maxRetries = 3
    private val retryDelayMs = 2000L
    private val logger: Logger = Logger.getLogger(TAG)

    @Bean
    open fun milvusClient(): MilvusClientV2 {
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                return MilvusClientV2(
                    ConnectConfig.builder()
                        .uri(ENDPOINT_URI)
                        .token(BEAR_TOKEN)
                        .connectTimeoutMs(10000)
                        .build()
                )
            } catch (ex: Exception) {
                attempt++
                if (attempt >= maxRetries) throw ex
                logger.warning("failed to instantiate Milvus Client. $attempt'th try...")
                Thread.sleep(retryDelayMs)
            }
        }
        throw IllegalStateException("Failed to instantiate Milvus Client.")
    }

    companion object {
        val TAG = "MilvusConfig"
    }
}