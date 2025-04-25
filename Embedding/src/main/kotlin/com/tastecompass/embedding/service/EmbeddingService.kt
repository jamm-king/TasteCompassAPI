package com.tastecompass.embedding.service

import com.tastecompass.embedding.client.OpenAIEmbedder
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.dto.EmbeddingRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmbeddingService(
    private val openaiClient: OpenAIEmbedder
) {
    suspend fun embed(
        embeddingRequest: EmbeddingRequest
    ): EmbeddingResult = coroutineScope {
        val moodVectorDeferred = async { openaiClient.embed(embeddingRequest.mood) }
        val tasteVectorDeferred = async { openaiClient.embed(embeddingRequest.taste) }

        val moodVector = moodVectorDeferred.await()
        val tasteVector = tasteVectorDeferred.await()

        EmbeddingResult(
            moodVector = moodVector,
            tasteVector = tasteVector
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}