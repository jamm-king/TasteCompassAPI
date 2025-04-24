package com.tastecompass.data.entity

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.common.Constants
import com.tastecompass.domain.entity.Restaurant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RestaurantTest {

    @Test
    fun `create should create new PREPARED entity`() {
        val restaurant = Restaurant.create(
            id = "test-1",
            status = AnalyzeStep.PREPARED,
            name = "name-1",
        )

        assertEquals("test-1", restaurant.id)
        assertEquals(AnalyzeStep.PREPARED, restaurant.status)
        assertEquals("name-1", restaurant.name)
    }

    @Test
    fun `update should return newly bounded entity with updated data`() {
        val restaurant = Restaurant.create(
            id = "test-1",
            status = AnalyzeStep.PREPARED,
            name = "name-1",
        )

        val updatedRestaurant = restaurant.update(
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )

        assertEquals(Constants.EMBEDDING_SIZE, updatedRestaurant.moodVector.size)
        assertEquals(0.2f, updatedRestaurant.moodVector[0])
        assertEquals(AnalyzeStep.EMBEDDED, updatedRestaurant.status)
    }
}