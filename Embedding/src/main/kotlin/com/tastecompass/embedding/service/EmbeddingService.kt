package com.tastecompass.embedding.service

import com.tastecompass.embedding.client.OpenAIEmbedder
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.dto.EmbeddingRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmbeddingService(
    private val openaiClient: OpenAIEmbedder
) {
    fun embed(embeddingRequest: EmbeddingRequest): EmbeddingResult {
        val moodVector = openaiClient.embed(embeddingRequest.mood)
        val tasteVector = openaiClient.embed(embeddingRequest.taste)

        return EmbeddingResult(
            moodVector = moodVector,
            tasteVector = tasteVector
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}