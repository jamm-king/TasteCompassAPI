package com.tastecompass.data.entity

import com.tastecompass.data.common.AnalyzeStep
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RestaurantMetadataTest {

    @Test
    fun `fromDocument should return entity with given document`() {
        val document = Document().apply {
            put("id", "test-1")
            put("status", "PREPARED")
            put("name", "name-1")
        }

        val metadata = RestaurantMetadata.fromDocument(document)

        assertEquals("test-1", metadata.id)
        assertEquals(AnalyzeStep.PREPARED, metadata.status)
        assertEquals("name-1", metadata.name)
        assertEquals(RestaurantProperty.TASTE.defaultValue, metadata.taste)
        assertEquals(RestaurantProperty.MENUS.defaultValue, metadata.menus)
        assertEquals(RestaurantProperty.REVIEWS.defaultValue, metadata.reviews)
    }

    @Test
    fun `toDocument should return document of entity`() {
        val metadata = RestaurantMetadata(
            id = "test-1",
            status = AnalyzeStep.PREPARED,
            name = "name-1"
        )

        val document = metadata.toDocument()

        assertEquals("test-1", document["id"])
        assertEquals(AnalyzeStep.PREPARED, document["status"])
        assertEquals("name-1", document["name"])
    }

    @Test
    fun `update should return newly bounded object with updated data`() {
        val metadata = RestaurantMetadata(
            id = "test-1",
            status = AnalyzeStep.PREPARED,
            name = "name-1"
        )

        val updatedMetadata = metadata.update(
            status = AnalyzeStep.ANALYZED,
            reviews = listOf("taste good", "mood good")
        )

        assertEquals(AnalyzeStep.ANALYZED, updatedMetadata.status)
        assertTrue(updatedMetadata.reviews.any { it == "taste good" })
        assertTrue(updatedMetadata.reviews.any { it == "mood good" })
    }
}