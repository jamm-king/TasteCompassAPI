package com.tastecompass.data.entity

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tastecompass.data.common.Constants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RestaurantEmbeddingTest {

    @Test
    fun `fromMap should return object with given map`() {
        val map = mutableMapOf<String, Any>().apply {
            put("id", "test-1")
            put("moodVector", List(Constants.EMBEDDING_SIZE) { 0.2f })
        }

        val embedding = RestaurantEmbedding.fromMap(map)

        assertEquals("test-1", embedding.id)
        assertEquals(Constants.EMBEDDING_SIZE, embedding.moodVector.size)
        assertEquals(0.2f, embedding.moodVector[0])
    }

    @Test
    fun `toJsonObject should return JsonObject of entity`() {
        val embedding = RestaurantEmbedding(
            id = "test-1",
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )

        val jsonObject = embedding.toJsonObject()

        assertEquals("test-1", jsonObject.get("id").asString)
        assertEquals(Constants.EMBEDDING_SIZE, jsonObject.get("moodVector").asJsonArray.size())
        assertEquals(0.2f, jsonObject.get("moodVector").asJsonArray[0].asFloat)
    }

    @Test
    fun `update should return newly bounded object with updated data`() {
        val embedding = RestaurantEmbedding(
            id = "test-1",
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )

        val updatedEmbedding = embedding.update(moodVector = List(Constants.EMBEDDING_SIZE) { 0.9f })

        assertEquals(Constants.EMBEDDING_SIZE, updatedEmbedding.moodVector.size)
        assertEquals(0.9f, updatedEmbedding.moodVector[0])
    }
}