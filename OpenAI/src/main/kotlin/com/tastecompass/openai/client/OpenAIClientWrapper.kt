package com.tastecompass.openai.client

import com.openai.client.OpenAIClient
import org.springframework.stereotype.Component

@Component
interface OpenAIClientWrapper {
    fun chat(prompt: String): String
    fun embed(text: String): List<Double>
}