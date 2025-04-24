package com.tastecompass.analyzer.client

import com.openai.client.OpenAIClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.tastecompass.openai.common.OpenAIProperties
import org.springframework.stereotype.Component

@Component
class OpenAIChatter(
    private val openaiClient: OpenAIClient,
    private val openaiProperties: OpenAIProperties
) {
    fun chat(prompt: String): String {
        val params = ChatCompletionCreateParams.builder()
            .addUserMessage(prompt)
            .model(openaiProperties.chatModel)
            .build()
        val chatCompletion = openaiClient.chat().completions().create(params)


        return chatCompletion.choices().first().message().content().get()
    }
}