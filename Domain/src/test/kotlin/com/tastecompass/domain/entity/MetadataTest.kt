package com.tastecompass.domain.entity

import com.tastecompass.domain.common.AnalyzeStep
import org.bson.Document
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MetadataTest {

    @Test
    fun `fromDocument should return entity with given document`() {
        val document = Document().apply {
            put("id", "test-1")
            put("status", "PREPARED")
            put("name", "name-1")
        }

        val metadata = Metadata.fromDocument(document)

        assertEquals("test-1", metadata.id)
        assertEquals(AnalyzeStep.PREPARED, metadata.status)
        assertEquals("name-1", metadata.name)
        assertEquals(RestaurantProperty.TASTE.defaultValue, metadata.taste)
        assertEquals(RestaurantProperty.MENUS.defaultValue, metadata.menus)
        assertEquals(RestaurantProperty.REVIEWS.defaultValue, metadata.reviews)
    }

    @Test
    fun `toDocument should return document of entity`() {
        val metadata = Metadata(
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
        val metadata = Metadata(
            id = "test-1",
            status = AnalyzeStep.PREPARED,
            name = "name-1"
        )
        val review = Review(
            text = "This restaurant had amazing sushi!",
            source = "naver",
            address = "포항시 남구 이인로 90",
            url = "naver.com/test"
        )

        val updatedMetadata = metadata.update(
            status = AnalyzeStep.ANALYZED,
            reviews = listOf(review)
        )

        assertEquals(AnalyzeStep.ANALYZED, updatedMetadata.status)
        assertTrue(updatedMetadata.reviews.any { it.source == "naver" })
    }
}