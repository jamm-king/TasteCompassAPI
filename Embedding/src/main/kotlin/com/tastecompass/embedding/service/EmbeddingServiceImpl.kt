package com.tastecompass.embedding.service

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.mapper.EmbeddingRequestMapper
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
        restaurant: Restaurant
    ): EmbeddingResult = coroutineScope {
        try {
            val request = EmbeddingRequestMapper.fromRestaurant(restaurant)

            val moodVectorDeferred = async { client.embed(request.mood) }
            val tasteVectorDeferred = async { client.embed(request.taste) }

            logger.debug("Embedding... {}", restaurant)

            val moodVector = moodVectorDeferred.await()
            val tasteVector = tasteVectorDeferred.await()

            EmbeddingResult(
                moodVector = moodVector.map { it.toFloat() },
                tasteVector = tasteVector.map { it.toFloat() }
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