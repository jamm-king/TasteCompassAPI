package com.tastecompass.data.service

import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import com.tastecompass.data.saga.SagaCoordinator
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.common.Constants
import com.tastecompass.domain.entity.Embedding
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.domain.entity.Restaurant
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InOrder
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class RestaurantServiceMockTest {
    private lateinit var mongoRepo: MongoRepository<Metadata>
    private lateinit var milvusRepo: MilvusRepository<Embedding>
    private lateinit var saga: SagaCoordinator
    private lateinit var service: RestaurantService

    @BeforeEach
    fun setUp() {
        mongoRepo   = mock(MongoRepository::class.java) as MongoRepository<Metadata>
        milvusRepo  = mock(MilvusRepository::class.java) as MilvusRepository<Embedding>
        saga        = SagaCoordinator()
        service     = RestaurantService(mongoRepo, milvusRepo, saga)
    }

    @Test
    fun `should rollback mongo insert when milvus insert fails`() = runBlocking {
        // given
        val id        = "restaurant-1"
        val restaurant = Restaurant.create(
            id        = id,
            name = "Test Restaurant",
            tasteVector = List(1536) { 0.1f },
            status    = AnalyzeStep.EMBEDDED
        )
        val metadata = restaurant.metadata
        val embedding = restaurant.embedding!!

        whenever(mongoRepo.get(id))
            .thenThrow(EntityNotFoundException("not found"))
        whenever(milvusRepo.get(id))
            .thenReturn(embedding)

        whenever(mongoRepo.insert(metadata))
            .thenReturn(Unit)
        whenever(milvusRepo.insert(embedding))
            .thenThrow(RuntimeException("milvus failed"))
        whenever(mongoRepo.delete(id))
            .thenReturn(Unit)

        // when / then
        val ex = assertThrows<RuntimeException> {
            runBlocking { service.save(restaurant) }
        }
        assert(ex.message == "milvus failed")

        // verify sequence
        val inOrder: InOrder = inOrder(mongoRepo, milvusRepo)
        inOrder.verify(mongoRepo).insert(metadata)
        inOrder.verify(milvusRepo).insert(embedding)
        inOrder.verify(mongoRepo).delete(id)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun `should rollback mongo update when milvus upsert fails`() = runBlocking {
        // given
        val id        = "restaurant-1"
        val original = Restaurant.create(
            id        = id,
            name = "Test Restaurant",
            tasteVector = List(1536) { 0.1f },
            status    = AnalyzeStep.EMBEDDED
        )
        val updated = original.update(tasteVector = List(1536) { 0.2f })

        whenever(mongoRepo.get(id))
            .thenReturn(original.metadata)
        whenever(milvusRepo.get(id))
            .thenReturn(original.embedding)

        whenever(mongoRepo.update(updated.metadata))
            .thenReturn(Unit)
        whenever(milvusRepo.upsert(updated.embedding!!))
            .thenThrow(RuntimeException("milvus failed"))
        whenever(mongoRepo.update(original.metadata))
            .thenReturn(Unit)

        // when / then
        val ex = assertThrows<RuntimeException> {
            runBlocking { service.save(updated) }
        }
        assert(ex.message == "milvus failed")

        // verify sequence
        val inOrder: InOrder = inOrder(mongoRepo, milvusRepo)
        inOrder.verify(mongoRepo).update(updated.metadata)
        inOrder.verify(milvusRepo).upsert(updated.embedding!!)
        inOrder.verify(mongoRepo).update(original.metadata)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun `search returns list of Restaurant when hybridSearch and mongoRepo succeed`() = runBlocking {
        val dummyVector = listOf(0.1f, 0.2f, 0.3f)
        val fieldToVector = mapOf("tasteVector" to dummyVector)
        val topK = 2

        val embedding1 = Embedding(
            id = "r1",
            tasteVector = List(Constants.EMBEDDING_SIZE) { 0.2f },
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.5f }
        )
        val embedding2 = Embedding(
            id = "r2",
            tasteVector = List(Constants.EMBEDDING_SIZE) { 0.3f },
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.3f }
        )
        `when`(milvusRepo.hybridSearch(fieldToVector, topK))
            .thenReturn(listOf(embedding1, embedding2))

        val meta1 = Metadata(
            id = "r1",
            name = "Restaurant One",
            address = "Seoul",
            status = AnalyzeStep.EMBEDDED,
            taste = listOf("delicious"),
            mood = listOf("good")
        )
        val meta2 = Metadata(
            id = "r2",
            name = "Restaurant Two",
            address = "Busan",
            status = AnalyzeStep.EMBEDDED,
            taste = listOf("sweet"),
            mood = listOf("country")
        )
        `when`(mongoRepo.get(listOf("r1", "r2")))
            .thenReturn(listOf(meta1, meta2))

        val result: List<Restaurant> = service.hybridSearch(fieldToVector, topK)

        assertEquals(2, result.size)

        assertEquals("r1", result[0].id)
        assertEquals("Restaurant One", result[0].name)
        // embedding score나 다른 값도 필요하면 추가로 assert

        assertEquals("r2", result[1].id)
        assertEquals("Restaurant Two", result[1].name)
    }

    @Test
    fun `search throws exception when hybridSearch throws`(): Unit = runBlocking {
        val dummyVector = listOf(0.5f, 0.5f)
        val fieldToVector = mapOf("moodVector" to dummyVector)
        val topK = 3

        `when`(milvusRepo.hybridSearch(fieldToVector, topK))
            .thenThrow(RuntimeException("Milvus error"))

        try {
            service.hybridSearch(fieldToVector, topK)
        } catch (e: Exception) {
            assertEquals("Milvus error", e.message)
        }
    }
}