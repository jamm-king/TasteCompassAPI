package com.tastecompass.openai.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="openai")
class OpenAIProperties {
    lateinit var apiKey: String
    lateinit var chatModel: String
}