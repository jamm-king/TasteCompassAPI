package com.tastecompass.data.entity

import com.tastecompass.data.common.AnalyzeStep

class RestaurantMetadataSample {
    val idList = listOf("test-0", "test-1", "test-2")
    fun data(): List<RestaurantMetadata> {
        return listOf(
            RestaurantMetadata(
                id = idList[0],
                status = AnalyzeStep.PREPARED,
                source = "source-0",
                name = "name-0",
                category = "category-0",
                phone = "phone-0",
                address = "address-0",
                x = 0.0,
                y = 0.0,
                reviews = listOf("review-0...", "review-1..."),
                businessDays = "businessDays-0",
                url = "url-0",
                hasWifi = true,
                hasParking = true,
                menus = listOf(
                    RestaurantMenu("menu-0", 10000),
                    RestaurantMenu("menu-1", 100000)
                ),
                minPrice = 10000,
                maxPrice = 100000,
                mood = "mood-0",
                taste = "taste-0"
            ),
            RestaurantMetadata(
                id = idList[1],
                status = AnalyzeStep.PREPARED,
                source = "source-1",
                name = "name-1",
                category = "category-1",
                phone = "phone-1",
                address = "address-1",
                x = 125.0,
                y = 30.0,
                reviews = listOf("review-2...", "review-3..."),
                businessDays = "businessDays-1",
                url = "url-1",
                hasWifi = true,
                hasParking = false,
                menus = listOf(
                    RestaurantMenu("menu-2", 5000),
                    RestaurantMenu("menu-3", 20000)
                ),
                minPrice = 5000,
                maxPrice = 20000,
                mood = "mood-1",
                taste = "taste-1"
            ),
            RestaurantMetadata(
                id = idList[2],
                status = AnalyzeStep.PREPARED,
                source = "source-2",
                name = "name-2",
                category = "category-2",
                phone = "phone-2",
                address = "address-2",
                x = 10.0,
                y = 50.0,
                reviews = listOf("review-4...", "review-5..."),
                businessDays = "businessDays-2",
                url = "url-2",
                hasWifi = false,
                hasParking = false,
                menus = listOf(
                    RestaurantMenu("menu-4", 100),
                    RestaurantMenu("menu-5", 2000)
                ),
                minPrice = 100,
                maxPrice = 2000,
                mood = "mood-2",
                taste = "taste-2"
            )
        )
    }
}