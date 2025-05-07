package com.tastecompass.openai.client

import com.openai.client.OpenAIClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.embeddings.EmbeddingCreateParams
import com.openai.models.embeddings.EmbeddingModel
import com.tastecompass.domain.common.Constants
import com.tastecompass.openai.common.OpenAIProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OpenAIClientWrapperImpl(
    private val openaiClient: OpenAIClient,
    private val openaiProperties: OpenAIProperties
): OpenAIClientWrapper {
    override fun chat(prompt: String): String {
        return try {
            val params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(openaiProperties.chatModel)
                .build()
            val chatCompletion = openaiClient.chat().completions().create(params)

            chatCompletion.choices().first().message().content().get()
        } catch(e: Exception) {
            logger.error("Failed chat completion with openai client: ${e.message}")
            throw e
        }
    }

    override fun embed(text: String): List<Double> {
        return try {
            val params = EmbeddingCreateParams.builder()
                .input(text)
                .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                .dimensions(Constants.EMBEDDING_SIZE.toLong())
                .build();
            val embedding = openaiClient.embeddings().create(params)

            embedding.data().first().embedding()
        } catch(e: Exception) {
            logger.error("Failed embedding with openai client: ${e.message}")
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}