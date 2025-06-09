package com.tastecompass.embedding.service

import com.tastecompass.embedding.dto.EmbeddingRequest
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.openai.client.OpenAIClientWrapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmbeddingServiceImpl(
    private val client: OpenAIClientWrapper
): EmbeddingService {
    override suspend fun embed(
        embeddingReq: EmbeddingRequest
    ): EmbeddingResult = coroutineScope {
        try {
            val moodVectorDeferred = async { client.embed(embeddingReq.mood) }
            val tasteVectorDeferred = async { client.embed(embeddingReq.taste) }
            val categoryVectorDeferred = async { client.embed(embeddingReq.category) }

            logger.debug("Embedding... {}", embeddingReq)

            val moodVector = moodVectorDeferred.await()
            val tasteVector = tasteVectorDeferred.await()
            val categoryVector = categoryVectorDeferred.await()

            EmbeddingResult(
                moodVector = moodVector.map { it.toFloat() },
                tasteVector = tasteVector.map { it.toFloat() },
                categoryVector = categoryVector.map { it.toFloat() }
            )
        } catch(e: Exception) {
            logger.error("Failed to embed restaurant: ${e.message}")
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}