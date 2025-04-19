package com.tastecompass.data.repository.milvus

import com.tastecompass.data.common.Constants
import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.data.entity.RestaurantEmbedding
import com.tastecompass.data.entity.RestaurantProperty
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.logging.Logger

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MilvusConfig::class, RestaurantEmbeddingRepository::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestaurantEmbeddingRepositoryIntegrativeTest {

    @Autowired
    private lateinit var repository: RestaurantEmbeddingRepository

    private val logger = Logger.getLogger(RestaurantEmbeddingRepositoryIntegrativeTest::class.simpleName)

    private val insertedIds = mutableListOf<String>()
    private val testId1 = "restaurant-1"
    private val testId2 = "restaurant-2"
    private val newId = "restaurant-new"

    @BeforeEach
    fun setup(): Unit = runBlocking {
        val embedding1 = RestaurantEmbedding(
            id = testId1,
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )
        val embedding2 = RestaurantEmbedding(
            id = testId2,
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.5f }
        )

        repository.delete(listOf(testId1, testId2, newId))
        repository.insert(listOf(embedding1, embedding2))
        insertedIds.addAll(listOf(testId1, testId2))

        Thread.sleep(500)
    }

    @AfterEach
    fun cleanup(): Unit = runBlocking {
        Thread.sleep(500)

        repository.delete(insertedIds)
        insertedIds.clear()
    }

    @Test
    fun `should return a restaurant embedding with given id`() = runBlocking {
        val result = repository.get(testId1)

        assertEquals(testId1, result.id)
    }

    @Test
    fun `should return list of restaurant embeddings matching given ids`() = runBlocking {
        val result = repository.get(listOf(testId1, testId2))

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == testId1 })
        assertTrue(result.any { it.id == testId2 })
        assertTrue(result.all { it.category == RestaurantProperty.CATEGORY.defaultValue })
        assertTrue(result.all { it.tasteVector == RestaurantProperty.TASTE_VECTOR.defaultValue})
    }

    @Test
    fun `should throw exception when getting not existing id`() = runBlocking {
        val wrongId = "id-wrong"

        val exception = assertThrows<EntityNotFoundException> {
            val result = repository.get(wrongId)
        }
        logger.info(exception.message)
    }

    @Test
    fun `should return list of whole restaurant embeddings`() = runBlocking {
        val result = repository.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should return list of embeddings with proximate vector`() = runBlocking {
        val queryVector = List(Constants.EMBEDDING_SIZE) { 0.2f }

        val result = repository.search(
            fieldName="moodVector",
            topK=2,
            vector=queryVector
        )

        assertEquals(2, result.size)
        assertTrue(result[0].id == testId1)
        assertTrue(result[1].id == testId2)
    }

    @Test
    fun`should insert new data`() = runBlocking {
        val newVector = List(Constants.EMBEDDING_SIZE) { 0.1f }
        val newEmbedding = RestaurantEmbedding(
            id = newId,
            moodVector = newVector
        )

        repository.insert(newEmbedding)
        insertedIds.add(newId)

        val result = repository.get(newId)
        assertEquals(newId, result.id)
        assertEquals(newVector, result.moodVector)
    }

    @Test
    fun `should throw exception when inserting an existing id`(): Unit = runBlocking {
        val newVector = List(Constants.EMBEDDING_SIZE) { 0.1f }
        val newEmbedding = RestaurantEmbedding(
            id = testId1,
            moodVector = newVector
        )

        val exception = assertThrows<InvalidRequestException> {
            repository.insert(newEmbedding)
            insertedIds.add(newId)
        }
        logger.info(exception.message)
    }

    @Test
    fun `should upsert new data`() = runBlocking {
        val newVector = List(Constants.EMBEDDING_SIZE) { 0.1f }
        val newEmbedding = RestaurantEmbedding(
            id = newId,
            moodVector = newVector
        )

        repository.upsert(newEmbedding)
        insertedIds.add(newId)

        val result = repository.get(newId)
        assertEquals(newId, result.id)
        assertEquals(newVector, result.moodVector)
    }

    @Test
    fun `should upsert present data`() = runBlocking {
        val restaurantEmbedding = repository.get(testId1)
        val updatedRestaurantEmbedding = restaurantEmbedding.update(
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.9f }
        )


        repository.upsert(listOf(updatedRestaurantEmbedding))

        val result = repository.get(listOf(testId1))
        assertEquals(0.9f, result.first().moodVector[0])
    }

    @Test
    fun `should delete present data`(): Unit = runBlocking {
        repository.delete(testId1)

        assertThrows<EntityNotFoundException> {
            repository.get(testId1)
        }
    }
}