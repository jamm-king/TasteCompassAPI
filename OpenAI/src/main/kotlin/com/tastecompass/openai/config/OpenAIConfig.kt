package com.tastecompass.openai.config

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.tastecompass.openai.common.OpenAIProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:openai.properties")
@EnableConfigurationProperties(OpenAIProperties::class)
open class OpenAIConfig(
    private val openaiProperties: OpenAIProperties
) {
    @Bean
    open fun openaiClient(): OpenAIClient {
        return OpenAIOkHttpClient.builder()
            .apiKey(openaiProperties.apiKey)
            .build()
    }
}