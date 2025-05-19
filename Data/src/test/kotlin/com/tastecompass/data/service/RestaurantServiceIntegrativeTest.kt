package com.tastecompass.data.service

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.common.Constants
import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.data.exception.InvalidRequestException
import com.tastecompass.data.repository.milvus.EmbeddingRepository
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MetadataRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import com.tastecompass.data.saga.SagaCoordinator
import com.tastecompass.domain.entity.Embedding
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.domain.entity.Restaurant
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[
    MilvusConfig::class, MongoConfig::class,
    SagaCoordinator::class,
    EmbeddingRepository::class, MetadataRepository::class,
    RestaurantService::class])
class RestaurantServiceIntegrativeTest {

    @Autowired
    private lateinit var dataService: DataService<Restaurant>
    @Autowired
    private lateinit var mongoRepository: MongoRepository<Metadata>
    @Autowired
    private lateinit var milvusRepository: MilvusRepository<Embedding>

    private val insertedIds = mutableListOf<String>()
    private val testId1 = "restaurant-1"
    private val testId2 = "restaurant-2"

    @AfterEach
    fun cleanup() = runBlocking {
        milvusRepository.delete(insertedIds)
        mongoRepository.delete(insertedIds)

        Thread.sleep(100)
    }

    @Test
    fun `should save EMBEDDED entity`() = runBlocking {
        val metadata = Metadata(id = testId1, status = AnalyzeStep.EMBEDDED)
        val embedding = Embedding(id = testId1)
        val restaurant = Restaurant.create(
            metadata = metadata,
            embedding = embedding
        )

        dataService.save(restaurant)
        insertedIds.add(testId1)

        Thread.sleep(1000)

        val result = dataService.getById(testId1)
        assertEquals(testId1, result.id)
    }

    @Test
    fun `should throw exception when saving PREPARED or ANALYZED entity`(): Unit = runBlocking {
        val restaurant = Restaurant.create(
            id = testId1,
            status = AnalyzeStep.PREPARED
        )

        assertThrows<InvalidRequestException> {
            dataService.save(restaurant)
            insertedIds.add(testId1)
        }
    }

    @Test
    fun `should search proximate entity with given vector`() = runBlocking {
        val metadata1 = Metadata(id = testId1, status = AnalyzeStep.EMBEDDED)
        val embedding1 = Embedding(id = testId1, moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f })
        val restaurant1 = Restaurant.create(
            metadata = metadata1,
            embedding = embedding1
        )
        val metadata2 = Metadata(id = testId2, status = AnalyzeStep.EMBEDDED)
        val embedding2 = Embedding(id = testId2, moodVector = List(Constants.EMBEDDING_SIZE) { 0.5f })
        val restaurant2 = Restaurant.create(
            metadata = metadata2,
            embedding = embedding2
        )
        dataService.save(restaurant1)
        dataService.save(restaurant2)
        insertedIds.addAll(listOf(testId1, testId2))
        Thread.sleep(1000)

        val result = dataService.search(
            fieldName = "moodVector",
            topK = 2,
            vector = List(Constants.EMBEDDING_SIZE) { 0.5f }
        )
        Thread.sleep(1000)

        assertEquals(testId2, result[0].id)
        assertEquals(testId1, result[1].id)
    }

    @Test
    fun `should get restaurant with given id`(): Unit = runBlocking {
        val name = "restaurant-name"
        val metadata = Metadata(id = testId1, status = AnalyzeStep.EMBEDDED, name = name)
        val embedding = Embedding(id = testId1)
        val restaurant = Restaurant.create(
            metadata = metadata,
            embedding = embedding
        )
        dataService.save(restaurant)
        insertedIds.add(testId1)

        val result = dataService.getById(testId1)
        Thread.sleep(1000)

        assertEquals(testId1, result.id)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}