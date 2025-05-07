package com.tastecompass.embedding.mapper

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingRequest

object EmbeddingRequestMapper {
    fun fromRestaurant(restaurant: Restaurant): EmbeddingRequest {
        return EmbeddingRequest(
            mood = restaurant.mood.last(),
            taste = restaurant.taste.last()
        )
    }
}