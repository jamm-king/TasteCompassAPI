package com.tastecompass.data.entity

import com.tastecompass.data.common.Constants

class RestaurantEmbeddingSample {
    val idList = listOf("test-0", "test-1")
    fun data(): List<RestaurantEmbedding> {
        return listOf(
            RestaurantEmbedding(
                id = idList[0],
                category = "category-0",
                address = "address-0",
                x = 0.0,
                y = 0.0,
                businessDays = "businessDays-0",
                hasWifi = true,
                hasParking = true,
                minPrice = 10000,
                maxPrice = 100000,
                moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f },
                tasteVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
            ),
            RestaurantEmbedding(
                id = idList[1],
                category = "category-1",
                address = "address-1",
                x = 125.0,
                y = 30.0,
                businessDays = "businessDays-1",
                hasWifi = true,
                hasParking = false,
                minPrice = 5000,
                maxPrice = 20000,
                moodVector = List(Constants.EMBEDDING_SIZE) { 0.5f },
                tasteVector = List(Constants.EMBEDDING_SIZE) { 0.5f }
            )
        )
    }
}