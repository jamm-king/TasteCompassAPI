package com.tastecompass.service

import com.tastecompass.analyzer.dto.QueryAnalysisResult
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

            val autoFieldToWeight = determineFieldWeights(analysisResult)

            val fieldToWeight: Map<String, Float> = mapOf(
                "tasteVector"    to resolveWeight(tasteWeight, autoFieldToWeight["tasteVector"]!!),
                "categoryVector" to resolveWeight(categoryWeight, autoFieldToWeight["categoryVector"]!!),
                "moodVector"     to resolveWeight(moodWeight, autoFieldToWeight["moodVector"]!!)
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

    fun determineFieldWeights(analysisResult: QueryAnalysisResult): Map<String, Float> {
        val baseWeights = mutableMapOf(
            "tasteVector" to 1.0f,
            "categoryVector" to 1.0f,
            "moodVector" to 1.0f
        )

        // Category weight scaling
        if (!analysisResult.category.isNullOrBlank() && analysisResult.categoryConfidence != null) {
            // Boost more if confidence high
            val categoryBoost = when {
                analysisResult.categoryConfidence >= 0.9f -> 3.0f
                analysisResult.categoryConfidence >= 0.7f -> 2.5f
                analysisResult.categoryConfidence >= 0.5f -> 2.0f
                else -> 1.0f // Low confidence → no boost
            }
            baseWeights["categoryVector"] = categoryBoost
        }

        // Mood weight scaling
        if (!analysisResult.mood.isNullOrBlank() && analysisResult.moodConfidence != null) {
            val moodBoost = when {
                analysisResult.moodConfidence >= 0.9f -> 2.0f
                analysisResult.moodConfidence >= 0.7f -> 1.8f
                analysisResult.moodConfidence >= 0.5f -> 1.5f
                else -> 1.0f
            }
            baseWeights["moodVector"] = moodBoost
        }

        // Taste weight scaling
        if (!analysisResult.taste.isNullOrBlank() && analysisResult.tasteConfidence != null) {
            val tasteBoost = when {
                analysisResult.tasteConfidence >= 0.9f -> 2.0f
                analysisResult.tasteConfidence >= 0.7f -> 1.8f
                analysisResult.tasteConfidence >= 0.5f -> 1.5f
                else -> 1.0f
            }
            baseWeights["tasteVector"] = tasteBoost
        }

        // Optional: normalize total sum to 3.0 for stability
        val sumWeights = baseWeights.values.sum()
        val normalizedWeights = baseWeights.mapValues { (_, value) ->
            value * (3.0f / sumWeights)
        }

        logger.debug("Auto-determined field weights (confidence-based): {}", normalizedWeights)

        return normalizedWeights
    }



    private fun resolveWeight(manualWeight: Float, autoWeight: Float): Float {
        return if (manualWeight != 1.0f) manualWeight else autoWeight
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
