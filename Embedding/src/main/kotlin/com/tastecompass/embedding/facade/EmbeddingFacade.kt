package com.tastecompass.embedding.facade

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.mapper.EmbeddingRequestMapper
import com.tastecompass.embedding.service.EmbeddingService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EmbeddingFacade(
    private val embeddingService: EmbeddingService
) {
    suspend fun embedRestaurant(
        restaurant: Restaurant
    ): EmbeddingResult = coroutineScope {
        val request = EmbeddingRequestMapper.fromRestaurant(restaurant)
        val embeddingDeferred = async { embeddingService.embed(request) }
        logger.info("Embedding... ${restaurant.toString()}")

        embeddingDeferred.await()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}