package com.tastecompass.data.service

import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.data.common.AnalyzeStep
import com.tastecompass.data.common.Constants
import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.data.entity.*
import com.tastecompass.data.repository.milvus.RestaurantEmbeddingRepository
import com.tastecompass.data.repository.mongo.RestaurantMetadataRepository
import io.milvus.v2.client.MilvusClientV2
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MilvusConfig::class, MongoConfig::class])
class RestaurantServiceIntegrativeTest {

    @Autowired
    private lateinit var milvusClient: MilvusClientV2
    @Autowired
    private lateinit var mongoClient: MongoClient
    private lateinit var restaurantEmbeddingRepository: RestaurantEmbeddingRepository
    private lateinit var restaurantMetadataRepository: RestaurantMetadataRepository
    private lateinit var restaurantService: RestaurantService

    private val insertedIds = mutableListOf<String>()
    private val restaurantSample = RestaurantSample()
    private val restaurantMetadataSample = RestaurantMetadataSample()
    private val restaurantEmbeddingSample = RestaurantEmbeddingSample()
    private val testId1 = restaurantSample.idList[0]
    private val testId2 = restaurantSample.idList[1]
    private val testId3 = restaurantSample.idList[2]

    @BeforeEach
    fun setup() {
        restaurantEmbeddingRepository = RestaurantEmbeddingRepository(milvusClient)
        restaurantMetadataRepository = RestaurantMetadataRepository((mongoClient))
        restaurantService = RestaurantService(restaurantMetadataRepository, restaurantEmbeddingRepository)

        val sampleData = restaurantSample.data()
        val restaurant1 = sampleData[0]
        val restaurant2 = sampleData[1]
        val restaurant3 = sampleData[2]
        val embeddingSampleData = restaurantEmbeddingSample.data()

        restaurantService.delete(listOf(testId1, testId2, testId3))
        restaurantService.insert(listOf(restaurant1, restaurant2, restaurant3))
        insertedIds.addAll(listOf(testId1, testId2, testId3))
        val updatedRestaurant1 = restaurant1.update(
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f },
            tasteVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )
        val updatedRestaurant2 = restaurant2.update(
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.5f },
            tasteVector = List(Constants.EMBEDDING_SIZE) { 0.5f }
        )
        restaurantService.update(listOf(updatedRestaurant1, updatedRestaurant2))

        Thread.sleep(100)
    }

    @AfterEach
    fun cleanup() {
        Thread.sleep(100)

        restaurantService.delete(insertedIds)
    }

    @Test
    fun `get should return list of restaurants with given ids`() {
        val result = restaurantService.get(listOf(testId1, testId2, testId3))

        assertEquals(3, result.size)
    }

    @Test
    fun `getAll should return list of whole restaurants`() {
        val result = restaurantService.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `insert should insert new prepared restaurants`() {
        val newId = "new restaurant"
        val newRestaurant = Restaurant(
            id = newId,
            metadata = RestaurantMetadata(
                id = newId,
                status = AnalyzeStep.PREPARED
            ),
            embedding = null,
        )

        restaurantService.insert(newRestaurant)

        val result = restaurantService.get(newId)
        assertEquals("new restaurant", result?.id)
    }

    @Test
    fun `update should update present restaurants`() {
        val updatedName = "updated restaurant"
        val restaurant = restaurantService.get(testId1)
        val updatedRestaurant = restaurant?.update(name = updatedName)

        restaurantService.update(updatedRestaurant ?: throw Exception())

        val result = restaurantService.get(testId1)
        assertEquals(updatedName, result?.name)
    }

    @Test
    fun `upsert should update present restaurants`() {
        val updatedName = "updated restaurant"
        val restaurant = restaurantService.get(testId1)
        val updatedRestaurant = restaurant?.update(name = updatedName)

        restaurantService.upsert(updatedRestaurant ?: throw Exception())

        val result = restaurantService.get(testId1)
        assertEquals(updatedName, result?.name)
    }

    @Test
    fun `upsert should insert new restaurants`() {
        val newId = "new restaurant"
        val newRestaurant = Restaurant(
            id = newId,
            metadata = RestaurantMetadata(
                id = newId,
                status = AnalyzeStep.PREPARED
            ),
            embedding = null,
        )

        restaurantService.upsert(newRestaurant)

        val result = restaurantService.get(newId)
        assertEquals("new restaurant", result?.id)
    }

    @Test
    fun `delete should delete present restaurants`() {
        restaurantService.delete(testId1)

        val result = restaurantService.get(testId1)
        assertTrue(result == null)
    }

    @Test
    fun `getAsFlow should return flow of restaurants`() {

    }
}