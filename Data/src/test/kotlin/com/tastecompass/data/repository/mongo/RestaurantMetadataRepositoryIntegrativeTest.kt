package com.tastecompass.data.repository.mongo

import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.data.common.AnalyzeStep
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.data.entity.RestaurantMetadata
import com.tastecompass.data.entity.RestaurantMetadataSample
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MongoConfig::class])
class RestaurantMetadataRepositoryIntegrativeTest {

    @Autowired
    private lateinit var mongoClient: MongoClient
    private lateinit var restaurantMetadataRepository: RestaurantMetadataRepository

    private val insertedIds = mutableListOf<String>()
    private val sample = RestaurantMetadataSample()
    private val testId1 = sample.idList[0]
    private val testId2 = sample.idList[1]

    @BeforeEach
    fun setup() {
        restaurantMetadataRepository = RestaurantMetadataRepository(mongoClient)

        val sampleData = sample.data()
        val restaurantMetadata1 = sampleData[0]
        val restaurantMetadata2 = sampleData[1]

        restaurantMetadataRepository.delete(listOf(testId1, testId2))
        restaurantMetadataRepository.insert(listOf(restaurantMetadata1, restaurantMetadata2))
        insertedIds.addAll(listOf(testId1, testId2))

        Thread.sleep(100)
    }

    @AfterEach
    fun cleanup() {
        Thread.sleep(100)

        if (insertedIds.isNotEmpty()) {
            restaurantMetadataRepository.delete(insertedIds)
            insertedIds.clear()
        }
    }

    @Test
    fun `get should return list of restaurant metadata`() {
        val result = restaurantMetadataRepository.get(listOf(testId1, testId2))

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == testId1 })
        assertTrue(result.any { it.id == testId2 })
    }

    @Test
    fun `getAll should return list of whole restaurant metadata`() {
        val result = restaurantMetadataRepository.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `insert should insert new data`() {
        val newTestId = "test-new"
        val newRestaurantMetadata = RestaurantMetadata(
            id = newTestId,
            name = "test name new",
            mood = "test mood new",
            status = AnalyzeStep.PREPARED
        )

        restaurantMetadataRepository.insert(listOf(newRestaurantMetadata))
        insertedIds.add(newTestId)

        val result = restaurantMetadataRepository.get(listOf(newTestId))
        assertEquals("test-new", result.first().id)
    }

    @Test
    fun `update should update present data`() {
        val updatedRestaurantMetadata = RestaurantMetadata(
            id = testId1,
            name = "test name updated",
            mood = "test mood updated",
            status = AnalyzeStep.ANALYZED
        )

        restaurantMetadataRepository.update(listOf(updatedRestaurantMetadata))

        val result = restaurantMetadataRepository.get(listOf(testId1))

        assertEquals("test name updated", result.first().name)
        assertEquals(AnalyzeStep.ANALYZED, result.first().status)
    }

    @Test
    fun `delete should delete present data`() {
        restaurantMetadataRepository.delete(listOf(testId1))

        val result = restaurantMetadataRepository.get(listOf(testId1))
        assertTrue(result.isEmpty())
    }
}