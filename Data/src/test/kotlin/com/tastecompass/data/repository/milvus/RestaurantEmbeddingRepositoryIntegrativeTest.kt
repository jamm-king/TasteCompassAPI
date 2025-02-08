package com.tastecompass.data.repository.milvus

import com.tastecompass.data.common.Constants
import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.data.entity.RestaurantEmbeddingSample
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
@ContextConfiguration(classes=[MilvusConfig::class])
class RestaurantEmbeddingRepositoryIntegrativeTest {

    @Autowired
    private lateinit var milvusClient: MilvusClientV2
    private lateinit var restaurantEmbeddingRepository: RestaurantEmbeddingRepository

    private val insertedIds = mutableListOf<String>()
    private val sample = RestaurantEmbeddingSample()
    private val testId1 = sample.idList[0]
    private val testId2 = sample.idList[1]

    @BeforeEach
    fun setup() {
        restaurantEmbeddingRepository = RestaurantEmbeddingRepository(milvusClient)

        val sampleData = sample.data()
        val restaurantEmbedding1 = sampleData[0]
        val restaurantEmbedding2 = sampleData[1]

        restaurantEmbeddingRepository.delete(listOf(testId1, testId2))
        restaurantEmbeddingRepository.insert(listOf(restaurantEmbedding1, restaurantEmbedding2))
        insertedIds.addAll(listOf(testId1, testId2))

        Thread.sleep(100)
    }

    @AfterEach
    fun cleanup() {
        Thread.sleep(100)

        restaurantEmbeddingRepository.delete(insertedIds)
        insertedIds.clear()
    }

    @Test
    fun `get should return list of restaurant embeddings`() {
        val result = restaurantEmbeddingRepository.get(listOf(testId1, testId2))

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == testId1 })
        assertTrue(result.any { it.id == testId2 })
    }

    @Test
    fun `getAll should return list of whole restaurant embeddings`() {
        val result = restaurantEmbeddingRepository.getAll()

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `search should return list of list of embeddings with proximate vector`() {
        val queryVector1 = List(Constants.EMBEDDING_SIZE) { 0.2f }
        val queryVector2 = List(Constants.EMBEDDING_SIZE) { 0.1f }

        val result = restaurantEmbeddingRepository.search(
            fieldName="moodVector",
            topK=2,
            vectorList=listOf(queryVector1, queryVector2)
        )

        println("query vectors :")
        println("#1 $queryVector1")
        println("#2 $queryVector2")
        println("search result#1")
        println("1. ${result[0][0]}")
        println("2. ${result[0][1]}")
        println("search result#2")
        println("1. ${result[1][0]}")
        println("2. ${result[1][1]}")

        assertEquals(2, result.size)
        assertEquals(2, result.first().size)
        assertTrue(result[0][0].id == testId1)
        assertTrue(result[1][0].id == testId2)
    }

    @Test
    fun `upsert should insert update present data`() {
        val restaurantEmbedding = restaurantEmbeddingRepository.get(listOf(testId1)).first()
        val updatedRestaurantEmbedding = restaurantEmbedding.update(
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.9f }
        )


        restaurantEmbeddingRepository.upsert(listOf(updatedRestaurantEmbedding))

        val result = restaurantEmbeddingRepository.get(listOf(testId1))
        assertEquals(0.9f, result.first().moodVector[0])
    }

    @Test
    fun `delete should delete present data`() {
        restaurantEmbeddingRepository.delete(listOf(testId1))

        val result = restaurantEmbeddingRepository.get(listOf(testId1))
        assertTrue(result.isEmpty())
    }
}