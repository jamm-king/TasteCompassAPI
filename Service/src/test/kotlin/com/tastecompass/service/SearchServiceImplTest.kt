package com.tastecompass.service

import com.google.gson.Gson
import com.tastecompass.analyzer.dto.QueryAnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingRequest
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import com.tastecompass.redis.client.RedisClientWrapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SearchServiceImplTest {

    @Mock lateinit var analyzerService: AnalyzerService
    @Mock lateinit var embeddingService: EmbeddingService
    @Mock lateinit var dataService: DataService<Restaurant>
    @Mock lateinit var redis: RedisClientWrapper

    private val gson = Gson()

    private lateinit var searchService: SearchServiceImpl

    @BeforeEach
    fun setup() {
        searchService = SearchServiceImpl(analyzerService, embeddingService, dataService, redis)
    }

    @Test
    fun `search returns restaurants when all dependencies succeed`() = runBlocking {
        val query = "spicy ramen"
        val topK = 3
        val tasteWeight = 2.0f
        val categoryWeight = 2.0f
        val moodWeight = 2.0f

        val analysisResult = QueryAnalysisResult(
            mood = "energetic",
            moodConfidence = 0.9f,
            taste = "spicy",
            tasteConfidence = 0.85f,
            category = "ramen",
            categoryConfidence = 0.7f,
            intent = "TASTE_FOCUSED"
        )
        val analysisJson = gson.toJson(analysisResult)
        `when`(redis.get(CacheKeyGenerator.analysisKey(query))).thenReturn(null)
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        val embeddingResult = EmbeddingResult(
            moodVector = listOf(0.1f, 0.2f, 0.3f),
            tasteVector = listOf(0.4f, 0.5f, 0.6f),
            categoryVector = listOf(0.7f, 0.8f, 0.9f)
        )
        val embeddingJson = gson.toJson(embeddingResult)
        `when`(redis.get(CacheKeyGenerator.embeddingKey(query))).thenReturn(null)
        `when`(
            embeddingService.embed(EmbeddingRequest("energetic", "spicy", "ramen"))
        ).thenReturn(embeddingResult)

        val expectedRestaurants = listOf(
            Restaurant.create(id = "r1", name = "Ramen House"),
            Restaurant.create(id = "r2", name = "Spicy Noodles")
        )
        `when`(
            dataService.hybridSearch(
                mapOf(
                    "tasteVector" to embeddingResult.tasteVector,
                    "moodVector" to embeddingResult.moodVector,
                    "categoryVector" to embeddingResult.categoryVector
                ),
                mapOf(
                    "tasteVector" to tasteWeight,
                    "moodVector" to moodWeight,
                    "categoryVector" to categoryWeight
                ),
                topK
            )
        ).thenReturn(expectedRestaurants)

        val result = searchService.search(query, topK, tasteWeight, categoryWeight, moodWeight)

        assertEquals(2, result.size)
        assertEquals("Ramen House", result[0].name)
        assertEquals("Spicy Noodles", result[1].name)
    }

    @Test
    fun `search returns empty list when analyzerService throws exception`() = runBlocking {
        val query = "sushi"
        `when`(redis.get(CacheKeyGenerator.analysisKey(query))).thenReturn(null)
        `when`(analyzerService.analyze(query)).thenThrow(RuntimeException("Analyzer failure"))

        val result = searchService.search(query, 3, 1.0f, 1.0f, 1.0f)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list when embeddingService throws exception`() = runBlocking {
        val query = "bbq wings"
        val analysisResult = QueryAnalysisResult(
            mood = "hungry",
            moodConfidence = 0.8f,
            taste = "savory",
            tasteConfidence = 0.9f,
            category = "bbq",
            categoryConfidence = 0.6f,
            intent = "TASTE_FOCUSED"
        )
        `when`(redis.get(CacheKeyGenerator.analysisKey(query))).thenReturn(null)
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        `when`(redis.get(CacheKeyGenerator.embeddingKey(query))).thenReturn(null)
        `when`(
            embeddingService.embed(EmbeddingRequest("hungry", "savory", "bbq"))
        ).thenThrow(RuntimeException("Embedding failure"))

        val result = searchService.search(query, 3, 1.0f, 1.0f, 1.0f)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list when dataService throws exception`() = runBlocking {
        val query = "pasta"
        val analysisResult = QueryAnalysisResult(
            mood = "cozy",
            moodConfidence = 0.7f,
            taste = "cheesy",
            tasteConfidence = 0.9f,
            category = "italian",
            categoryConfidence = 0.85f,
            intent = "CATEGORY_FOCUSED"
        )
        `when`(redis.get(CacheKeyGenerator.analysisKey(query))).thenReturn(null)
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        val embeddingResult = EmbeddingResult(
            moodVector = listOf(0.1f, 0.2f),
            tasteVector = listOf(0.3f, 0.4f),
            categoryVector = listOf(0.5f, 0.6f)
        )
        `when`(redis.get(CacheKeyGenerator.embeddingKey(query))).thenReturn(null)
        `when`(
            embeddingService.embed(EmbeddingRequest("cozy", "cheesy", "italian"))
        ).thenReturn(embeddingResult)

        `when`(
            dataService.hybridSearch(
                anyMap(), anyMap(), anyInt()
            )
        ).thenThrow(RuntimeException("Search failed"))

        val result = searchService.search(query, 3, 1.0f, 1.0f, 1.0f)

        assertTrue(result.isEmpty())
    }
}

