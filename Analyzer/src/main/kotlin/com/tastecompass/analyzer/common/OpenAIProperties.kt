package com.tastecompass.analyzer.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix="openai")
class OpenAIProperties {
    lateinit var apiKey: String
    lateinit var chatModel: String
}