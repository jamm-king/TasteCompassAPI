package com.tastecompass.embedding.service

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingResult

interface EmbeddingService {
    suspend fun embed(restaurant: Restaurant): EmbeddingResult
}