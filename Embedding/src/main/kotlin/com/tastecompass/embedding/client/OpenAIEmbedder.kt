package com.tastecompass.embedding.client

import com.openai.client.OpenAIClient
import com.openai.models.embeddings.EmbeddingCreateParams
import com.openai.models.embeddings.EmbeddingModel
import com.tastecompass.domain.common.Constants
import org.springframework.stereotype.Component

@Component
class OpenAIEmbedder(
    private val openaiClient: OpenAIClient
) {
    fun embed(text: String): List<Double> {
        val params = EmbeddingCreateParams.builder()
            .input(text)
            .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
            .dimensions(Constants.EMBEDDING_SIZE.toLong())
            .build();
        val embedding = openaiClient.embeddings().create(params)

        return embedding.data().first().embedding()
    }
}