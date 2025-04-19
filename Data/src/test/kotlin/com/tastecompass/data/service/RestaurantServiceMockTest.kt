package com.tastecompass.data.service

import com.tastecompass.data.common.AnalyzeStep
import com.tastecompass.data.common.Constants
import com.tastecompass.data.entity.*
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class RestaurantServiceMockTest {

    @Mock
    private lateinit var mongoRepository: MongoRepository<RestaurantMetadata>
    @Mock
    private lateinit var milvusRepository: MilvusRepository<RestaurantEmbedding>
    @InjectMocks
    private lateinit var restaurantService: RestaurantService

    val testId1 = "test-1"

    @Test
    fun `should return processed list of restaurants`() = runTest {
        val fieldName = "taste"
        val topK = 5
        val vector = listOf(0.1f, 0.2f, 0.3f)
        val mockEmbedding = RestaurantEmbedding(
            id = testId1,
            moodVector = List(Constants.EMBEDDING_SIZE) { 0.2f }
        )
        val mockMetadata = RestaurantMetadata(
            id = testId1,
            status = AnalyzeStep.PREPARED,
            name = "name-1"
        )

        `when`(milvusRepository.search(fieldName, topK, vector))
            .thenReturn(listOf(mockEmbedding))
        `when`(mongoRepository.get(listOf("1")))
            .thenReturn(listOf(mockMetadata))

        val result = restaurantService.search(fieldName, topK, vector)

        assertEquals(1, result.size)
        assertEquals("Restaurant A", result[0].name)
        verify(milvusRepository).search(fieldName, topK, vector)
        verify(mongoRepository).get(listOf("1"))
    }

    @Test
    fun `insert should insert only prepared restaurants`() = runTest {
        val preparedRestaurant = mock(Restaurant::class.java)
        val metadata = RestaurantMetadata("1", AnalyzeStep.PREPARED, name = "Prepared Restaurant")

        `when`(preparedRestaurant.metadata).thenReturn(metadata)
        `when`(preparedRestaurant.status).thenReturn(AnalyzeStep.PREPARED)

        restaurantService.insert(listOf(preparedRestaurant))

        verify(mongoRepository).insert(listOf(metadata))
        verifyNoInteractions(milvusRepository)
    }

    @Test
    fun `update should update analyzed and embedded restaurants`() = runTest {
        val analyzedRestaurant = mock(Restaurant::class.java)
        val embeddedRestaurant = mock(Restaurant::class.java)

        val analyzedMetadata = RestaurantMetadata("1", AnalyzeStep.ANALYZED, name = "Analyzed Restaurant")
        val embeddedMetadata = RestaurantMetadata("2", AnalyzeStep.EMBEDDED, name = "Embedded Restaurant")
        val embedding = RestaurantEmbedding("2", tasteVector = listOf(0.1f, 0.2f))

        `when`(analyzedRestaurant.metadata).thenReturn(analyzedMetadata)
        `when`(analyzedRestaurant.status).thenReturn(AnalyzeStep.ANALYZED)

        `when`(embeddedRestaurant.metadata).thenReturn(embeddedMetadata)
        `when`(embeddedRestaurant.embedding).thenReturn(embedding)
        `when`(embeddedRestaurant.status).thenReturn(AnalyzeStep.EMBEDDED)

        restaurantService.update(listOf(analyzedRestaurant, embeddedRestaurant))

        verify(mongoRepository).update(listOf(analyzedMetadata))
        verify(mongoRepository).update(listOf(embeddedMetadata))
        verify(milvusRepository).insert(listOf(embedding))
    }

    @Test
    fun `upsert should insert new and update existing restaurants`() = runTest {
        val newRestaurant = mock(Restaurant::class.java)
        val existingRestaurant = mock(Restaurant::class.java)

        val newMetadata = RestaurantMetadata("1", AnalyzeStep.PREPARED, name = "New Restaurant")
        val existingMetadata = RestaurantMetadata("2", AnalyzeStep.EMBEDDED, name = "Existing Restaurant")
        val existingEmbedding = RestaurantEmbedding("2", tasteVector = listOf(0.1f, 0.2f))

        `when`(newRestaurant.id).thenReturn("1")
        `when`(existingRestaurant.id).thenReturn("2")
        `when`(newRestaurant.metadata).thenReturn(newMetadata)
        `when`(newRestaurant.status).thenReturn(AnalyzeStep.PREPARED)

        `when`(existingRestaurant.metadata).thenReturn(existingMetadata)
        `when`(existingRestaurant.embedding).thenReturn(existingEmbedding)
        `when`(existingRestaurant.status).thenReturn(AnalyzeStep.EMBEDDED)

        `when`(mongoRepository.get(listOf("1", "2"))).thenReturn(listOf(existingMetadata))

        restaurantService.upsert(listOf(newRestaurant, existingRestaurant))

        verify(mongoRepository).insert(listOf(newMetadata))
        verify(mongoRepository).update(listOf(existingMetadata))
        verify(milvusRepository).insert(listOf(existingEmbedding))
    }

    @Test
    fun `delete should call repositories with given IDs`() = runTest {
        val idList = listOf("1", "2")

        restaurantService.delete(idList)

        verify(mongoRepository).delete(idList)
        verify(milvusRepository).delete(idList)
    }

    @Test
    fun `get should return restaurants with combined metadata and embeddings`() = runTest {
        val idList = listOf("1")
        val metadata = RestaurantMetadata("1", AnalyzeStep.EMBEDDED, name = "Restaurant A")
        val embedding = RestaurantEmbedding("1", tasteVector = listOf(0.1f, 0.2f))

        `when`(mongoRepository.get(idList)).thenReturn(listOf(metadata))
        `when`(milvusRepository.get(idList)).thenReturn(listOf(embedding))

        val result = restaurantService.get(idList)

        assertEquals(1, result.size)
        assertEquals("Restaurant A", result[0].name)
        assertEquals(listOf(0.1f, 0.2f), result[0].tasteVector)
    }

    @Test
    fun `getAll should return all restaurants`() = runTest {
        val metadata = RestaurantMetadata("1", AnalyzeStep.EMBEDDED, name = "Restaurant A")
        val embedding = RestaurantEmbedding("1", tasteVector = listOf(0.1f, 0.2f))

        `when`(mongoRepository.getAll()).thenReturn(listOf(metadata))
        `when`(milvusRepository.getAll()).thenReturn(listOf(embedding))

        val result = restaurantService.getAll()

        assertEquals(1, result.size)
        assertEquals("Restaurant A", result[0].name)
        assertEquals(listOf(0.1f, 0.2f), result[0].tasteVector)
    }
}
