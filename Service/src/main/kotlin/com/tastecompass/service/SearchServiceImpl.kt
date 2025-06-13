package com.tastecompass.service

import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.embedding.dto.EmbeddingRequest
import com.tastecompass.embedding.service.EmbeddingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl(
    private val analyzerService: AnalyzerService,
    private val embeddingService: EmbeddingService,
    private val dataService: DataService<Restaurant>
) : SearchService {

    override suspend fun search(
        query: String,
        topK: Int,
        tasteWeight: Float,
        categoryWeight: Float,
        moodWeight: Float
    ): List<Restaurant> {
        logger.debug(
            "SearchService.search() called – query='{}', topK={}, tasteWeight={}, categoryWeight={}, moodWeight={}",
            query, topK, tasteWeight, categoryWeight, moodWeight
        )

        return try {
            val analysisResult = analyzerService.analyze(query)
            logger.debug(
                "Query analysis completed – mood='{}', taste='{}', category='{}'",
                analysisResult.mood,
                analysisResult.taste,
                analysisResult.category
            )

            val embeddingReq = EmbeddingRequest(
                mood = analysisResult.mood.orEmpty(),
                taste = analysisResult.taste.orEmpty(),
                category = analysisResult.category.orEmpty()
            )

            val embeddingResult = embeddingService.embed(embeddingReq)
            logger.debug(
                "Embedding generated – tasteVector size={}, moodVector size={}, categoryVector size={}",
                embeddingResult.tasteVector.size,
                embeddingResult.moodVector.size,
                embeddingResult.categoryVector.size
            )

            val fieldToVector: Map<String, List<Float>> = mapOf(
                "tasteVector"    to embeddingResult.tasteVector,
                "moodVector"     to embeddingResult.moodVector,
                "categoryVector" to embeddingResult.categoryVector
            )

            val fieldToWeight: Map<String, Float> = mapOf(
                "tasteVector"    to tasteWeight,
                "categoryVector" to categoryWeight,
                "moodVector"     to moodWeight
            )

            val searchResults: List<Restaurant> =
                dataService.hybridSearch(fieldToVector, fieldToWeight, topK)
            logger.debug("Hybrid search returned {} results", searchResults.size)

            searchResults
        } catch (e: Exception) {
            logger.error("Exception in SearchService.search: {}", e.message, e)
            emptyList()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
