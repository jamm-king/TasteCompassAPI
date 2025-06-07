package com.tastecompass.service

import com.tastecompass.analyzer.dto.QueryAnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingRequest
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SearchServiceImplTest {

    @Mock
    private lateinit var analyzerService: AnalyzerService

    @Mock
    private lateinit var embeddingService: EmbeddingService

    @Mock
    private lateinit var dataService: DataService<Restaurant>

    @InjectMocks
    private lateinit var searchService: SearchServiceImpl

    @Test
    fun `search returns restaurants when all dependencies succeed`(): Unit = runBlocking {
        // Given
        val query = "spicy ramen"
        val topK = 3

        val analysisResult = QueryAnalysisResult(mood = "energetic", taste = "spicy")
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        val expectedEmbedding = EmbeddingResult(
            moodVector = listOf(0.1f, 0.2f, 0.3f),
            tasteVector = listOf(0.4f, 0.5f, 0.6f)
        )
        `when`(embeddingService.embed(EmbeddingRequest("energetic", "spicy")))
            .thenReturn(expectedEmbedding)

        val expectedRestaurants = listOf(
            Restaurant.create(id = "r1", name = "Ramen House"),
            Restaurant.create(id = "r2", name = "Spicy Noodles")
        )
        val expectedFieldToVector = mapOf(
            "taste" to expectedEmbedding.tasteVector,
            "mood"  to expectedEmbedding.moodVector
        )
        `when`(dataService.hybridSearch(expectedFieldToVector, topK))
            .thenReturn(expectedRestaurants)

        // When
        val actualResults = searchService.search(query, topK)

        // Then
        assertEquals(2, actualResults.size)
        assertEquals("Ramen House", actualResults[0].name)
        assertEquals("Spicy Noodles", actualResults[1].name)

        // verify that hybridSearch was called once
        verify(dataService, times(1)).hybridSearch(expectedFieldToVector, topK)
    }

    @Test
    fun `search returns empty list when analyzerService throws exception`(): Unit = runBlocking {
        // Given
        val query = "sushi"
        val topK = 2

        `when`(analyzerService.analyze(query))
            .thenThrow(RuntimeException("Analyzer failure"))

        // When
        val actualResults = searchService.search(query, topK)

        // Then
        assertTrue(actualResults.isEmpty())
    }

    @Test
    fun `search returns empty list when embeddingService throws exception`(): Unit = runBlocking {
        // Given
        val query = "bbq wings"
        val topK = 4

        val analysisResult = QueryAnalysisResult(mood = "hungry", taste = "savory")
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        `when`(embeddingService.embed(EmbeddingRequest("hungry", "savory")))
            .thenThrow(RuntimeException("Embedding failure"))

        // When
        val actualResults = searchService.search(query, topK)

        // Then
        assertTrue(actualResults.isEmpty())
    }

    @Test
    fun `search returns empty list when dataService throws exception`() = runBlocking {
        // Given
        val query = "pasta"
        val topK = 5

        val analysisResult = QueryAnalysisResult(mood = "cozy", taste = "cheesy")
        `when`(analyzerService.analyze(query)).thenReturn(analysisResult)

        val expectedEmbedding = EmbeddingResult(
            moodVector = listOf(0.7f, 0.8f),
            tasteVector = listOf(0.9f, 1.0f)
        )
        `when`(embeddingService.embed(EmbeddingRequest("cozy", "cheesy")))
            .thenReturn(expectedEmbedding)

        `when`(
            dataService.hybridSearch(
                mapOf(
                    "taste" to expectedEmbedding.tasteVector,
                    "mood" to expectedEmbedding.moodVector
                ),
                topK
            )
        ).thenThrow(RuntimeException("DataService failure"))

        // When
        val actualResults = searchService.search(query, topK)

        // Then
        assertTrue(actualResults.isEmpty())
    }
}