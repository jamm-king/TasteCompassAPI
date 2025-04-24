package com.tastecompass.data.repository.milvus

import com.tastecompass.data.config.MilvusConfig
import com.tastecompass.domain.entity.RestaurantEmbedding
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.vector.request.DeleteReq
import io.milvus.v2.service.vector.request.GetReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.UpsertReq
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.system.measureTimeMillis

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MilvusConfig::class, RestaurantEmbeddingRepository::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestaurantEmbeddingRepositoryPerformanceTest {

    @Autowired
    private lateinit var client: MilvusClientV2
    @Autowired
    private lateinit var repository: RestaurantEmbeddingRepository

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val insertedIds = mutableListOf<String>()
    private val entitySize = 1
    private val batchSize = 1

    @BeforeEach
    fun setup() = runBlocking {
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            insertedIds.add(id)
        }

        repository.delete(insertedIds, entitySize)
        insertedIds.clear()
    }

    @AfterEach
    fun cleanup() = runBlocking {
        repository.delete(insertedIds, entitySize)
        insertedIds.clear()
    }

    @Test
    fun `client get performance test`() = runBlocking {
        val id = "restaurant-1"
        val embedding = RestaurantEmbedding(id = id)
        val idList = listOf(id)
        val dataList = listOf(embedding.toJsonObject())
        val insertReq = InsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()
        val getReq = GetReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(idList)
            .build()
        client.insert(insertReq)

        val elapsed = measureTimeMillis {
            client.get(getReq)
        }
        logger.info("CLIENT:GET Execution time: $elapsed ms")
    }

    @Test
    fun `client insert performance test`() = runBlocking {
        val id = "restaurant-1"
        val embedding = RestaurantEmbedding(id = id)
        val dataList = listOf(embedding.toJsonObject())
        val insertReq = InsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()

        val elapsed = measureTimeMillis {
            client.insert(insertReq)
        }
        logger.info("CLIENT:INSERT Execution time: $elapsed ms")
    }

    @Test
    fun `client upsert performance test`() = runBlocking {
        val id = "restaurant-1"
        val embedding = RestaurantEmbedding(id = id)
        val dataList = listOf(embedding.toJsonObject())
        val upsertReq = UpsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()

        val elapsed = measureTimeMillis {
            client.upsert(upsertReq)
        }
        logger.info("CLIENT:UPSERT Execution time: $elapsed ms")
    }

    @Test
    fun `client delete performance test`() = runBlocking {
        val id = "restaurant-1"
        val embedding = RestaurantEmbedding(id = id)
        val dataList = listOf(embedding.toJsonObject())
        val insertReq = InsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()
        client.insert(insertReq)

        val idList = listOf(id)
        val deleteReq = DeleteReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(idList)
            .build()

        val elapsed = measureTimeMillis {
            client.delete(deleteReq)
        }
        logger.info("CLIENT:DELETE Execution time: $elapsed ms")
    }

    @Test
    fun `repository get performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val embeddingList = mutableListOf<RestaurantEmbedding>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val embedding = RestaurantEmbedding(id = id)
            idList.add(id)
            embeddingList.add(embedding)
            insertedIds.add(id)
        }
        repository.insert(embeddingList, batchSize)

        val elapsed = measureTimeMillis {
            repository.get(idList, batchSize)
        }

        logger.info("REPO:GET Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository insert performance test`() = runBlocking {
        val embeddingList = mutableListOf<RestaurantEmbedding>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val embedding = RestaurantEmbedding(id = id)
            embeddingList.add(embedding)
            insertedIds.add(id)
        }

        val elapsed = measureTimeMillis {
            repository.insert(embeddingList, batchSize)
        }

        logger.info("REPO:INSERT Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository upsert performance test`() = runBlocking {
        val embeddingList = mutableListOf<RestaurantEmbedding>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val embedding = RestaurantEmbedding(id = id)
            embeddingList.add(embedding)
            insertedIds.add(id)
        }

        val elapsed1 = measureTimeMillis {
            repository.upsert(embeddingList, batchSize)
        }
        val elapsed2 = measureTimeMillis {
            repository.upsert(embeddingList, batchSize)
        }

        logger.info("REPO:UPSERT Execution time: $elapsed1 ms (upsert NEW entities) (entitySize: $entitySize, batchSize: $batchSize)")
        logger.info("REPO:UPSERT Execution time: $elapsed2 ms (upsert PRESENT entities) (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository delete performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val embeddingList = mutableListOf<RestaurantEmbedding>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val embedding = RestaurantEmbedding(id = id)
            idList.add(id)
            embeddingList.add(embedding)
            insertedIds.add(id)
        }
        repository.insert(embeddingList, batchSize)

        val elapsed = measureTimeMillis {
            repository.delete(idList, batchSize)
        }

        logger.info("REPO:DELETE Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    companion object {
        private const val COLLECTION_NAME = "Restaurant"
    }
}