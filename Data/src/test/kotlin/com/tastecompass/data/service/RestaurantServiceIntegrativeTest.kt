package com.tastecompass.data.service

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.common.Constants
import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.data.entity.*
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import com.tastecompass.data.repository.milvus.RestaurantEmbeddingRepository
import com.tastecompass.data.repository.mongo.RestaurantMetadataRepository
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.RestaurantMenu
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.take
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MilvusConfig::class, MongoConfig::class,
RestaurantEmbeddingRepository::class, RestaurantMetadataRepository::class,
RestaurantService::class])
class RestaurantServiceIntegrativeTest {

    @Autowired
    private lateinit var restaurantService: RestaurantService

    private val insertedIds = mutableListOf<String>()
    private val testId1 = "restaurant-1"
    private val testId2 = "restaurant-2"
    private val testId3 = "restaurant-3"
    private val newId = "restaurant-new"
    private val flowTestId = "test-flow"

    @BeforeEach
    fun setup() = runBlocking {
        // setup test data
        // "restaurant-1", "restaurant-2" is set as EMBEDDED
        // "restaurant-3 is set as PREPARED

        val restaurant1 = Restaurant.create(
            id = testId1,
            status = AnalyzeStep.PREPARED,
            source = "source-1",
            name = "name-1",
            category = "category-1",
            phone = "phone-1",
            address = "address-1",
            x = 0.0,
            y = 0.0,
            reviews = listOf("review-1...", "review-2..."),
            businessDays = "businessDays-1",
            url = "url-1",
            hasWifi = true,
            hasParking = true,
            menus = listOf(
                RestaurantMenu("menu-1", 10000),
                RestaurantMenu("menu-2", 100000)
            ),
            minPrice = 10000,
            maxPrice = 100000,
            mood = "mood-1",
            taste = "taste-1"
        )
        val restaurant2 = Restaurant.create(
            id = testId2,
            status = AnalyzeStep.PREPARED,
            source = "source-2",
            name = "name-2",
            category = "category-2",
            phone = "phone-2",
            address = "address-2",
            x = 125.0,
            y = 30.0,
            reviews = listOf("review-3...", "review-4..."),
            businessDays = "businessDays-2",
            url = "url-2",
            hasWifi = true,
            hasParking = false,
            menus = listOf(
                RestaurantMenu("menu-3", 5000),
                RestaurantMenu("menu-4", 20000)
            ),
            minPrice = 5000,
            maxPrice = 20000,
            mood = "mood-2",
            taste = "taste-2"
        )
        val restaurant3 = Restaurant.create(
            id = testId3,
            status = AnalyzeStep.PREPARED,
            source = "source-3",
            name = "name-3",
            category = "category-3",
            phone = "phone-3",
            address = "address-3",
            x = 10.0,
            y = 50.0,
            reviews = listOf("review-5...", "review-6..."),
            businessDays = "businessDays-3",
            url = "url-3",
            hasWifi = false,
            hasParking = false,
            menus = listOf(
                RestaurantMenu("menu-5", 100),
                RestaurantMenu("menu-6", 2000)
            ),
            minPrice = 100,
            maxPrice = 2000,
            mood = "mood-3",
            taste = "taste-3"
        )

        restaurantService.delete(listOf(testId1, testId2, testId3, newId, flowTestId))
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
    fun cleanup() = runBlocking {
        Thread.sleep(100)

        restaurantService.delete(insertedIds)
    }

    @Test
    fun `should return list of restaurants with given ids`() = runBlocking {
        val result = restaurantService.get(listOf(testId1, testId2, testId3))

        assertEquals(3, result.size)
    }

    @Test
    fun `should return list of whole restaurants`() = runBlocking {
        val result = restaurantService.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should insert new prepared restaurants`() = runBlocking {
        val newRestaurant = Restaurant.create(
            id = newId,
            status = AnalyzeStep.PREPARED
        )

        restaurantService.insert(newRestaurant)
        insertedIds.add(newId)

        val result = restaurantService.get(newId)
        assertEquals(newId, result.id)
    }

    @Test
    fun `should throw duplicate key exception when given existing id`() = runBlocking {
        val newRestaurant = Restaurant.create(
            id = testId1,
            status = AnalyzeStep.PREPARED
        )

        val exception = assertThrows<InvalidRequestException> {
            restaurantService.insert(newRestaurant)
            insertedIds.add(testId1)
        }
    }

    @Test
    fun `should throw invalid request exception when given ANALYZED or EMBEDDED to insert`() = runBlocking {
        val newRestaurant1 = Restaurant.create(
            id = newId,
            status = AnalyzeStep.ANALYZED
        )
        val newRestaurant2 = Restaurant.create(
            id = newId,
            status = AnalyzeStep.EMBEDDED
        )

        val exception1 = assertThrows<InvalidRequestException> {
            restaurantService.insert(newRestaurant1)
            insertedIds.add(newId)
        }
        val exception2 = assertThrows<InvalidRequestException> {
            restaurantService.insert(newRestaurant2)
            insertedIds.add(newId)
        }
    }

    @Test
    fun `should update present restaurants`() = runBlocking {
        val updatedName = "updated restaurant"
        val restaurant = restaurantService.get(testId1)
        val updatedRestaurant = restaurant.update(name = updatedName, status = AnalyzeStep.ANALYZED)

        restaurantService.update(updatedRestaurant)

        val result = restaurantService.get(testId1)
        assertEquals(updatedName, result.name)
    }

    @Test
    fun `should throw invalid request exception when given PREPARED to update`() = runBlocking {
        val updatedName = "updated restaurant"
        val preparedRestaurant = restaurantService.get(testId3)
        val updatedRestaurant = preparedRestaurant.update(name = updatedName)

        val exception = assertThrows<InvalidRequestException> {
            restaurantService.update(updatedRestaurant)
        }
    }

    @Test
    fun `should upsert new restaurants`() = runBlocking {
        val newRestaurant = Restaurant.create(
            id = newId,
            status = AnalyzeStep.PREPARED
        )

        restaurantService.upsert(newRestaurant)
        insertedIds.add(newId)

        val result = restaurantService.get(newId)
        assertEquals(newId, result.id)
    }

    @Test
    fun `should upsert present restaurants`() = runBlocking {
        val updatedName = "updated Restaurant"
        val restaurant = restaurantService.get(testId1)
        val updatedRestaurant = restaurant.update(name = updatedName, status = AnalyzeStep.ANALYZED)

        restaurantService.upsert(updatedRestaurant)

        val result = restaurantService.get(testId1)
        assertEquals(updatedName, result.name)
    }

    @Test
    fun `should delete present restaurants`() = runBlocking {
        restaurantService.delete(testId1)

        val exception = assertThrows<EntityNotFoundException> {
            val result = restaurantService.get(testId1)
        }
    }

    @Test
    fun `should return flow of restaurants`() = runBlocking {
        // Given
        val testRestaurant = Restaurant.create(
            id = flowTestId,
            status = AnalyzeStep.PREPARED,
            source = "source-flow",
            name = "name-flow",
            category = "category-flow",
            phone = "phone-flow",
            address = "address-flow",
            x = 0.0,
            y = 0.0,
            reviews = listOf("flow-review-1", "flow-review-2"),
            businessDays = "flow-businessDays",
            url = "flow-url",
            hasWifi = true,
            hasParking = false,
            menus = listOf(
                RestaurantMenu("flow-menu-1", 1000),
                RestaurantMenu("flow-menu-2", 2000)
            ),
            minPrice = 1000,
            maxPrice = 2000,
            mood = "flow-mood",
            taste = "flow-taste"
        )
        val collectedRestaurants = mutableListOf<Restaurant>()

        // When
        val job = launch {
            restaurantService.getAsFlow().take(1).collect { collectedRestaurants.add(it) }
        }

        restaurantService.insert(testRestaurant)
        insertedIds.add("test-flow")

        job.join()

        // Then
        assertEquals(1, collectedRestaurants.size)
        assertEquals(testRestaurant.id, collectedRestaurants[0].id)
        assertEquals(testRestaurant.name, collectedRestaurants[0].name)
    }
}