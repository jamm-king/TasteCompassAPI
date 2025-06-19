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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl(
    private val analyzerService: AnalyzerService,
    private val embeddingService: EmbeddingService,
    private val dataService: DataService<Restaurant>,
    private val redis: RedisClientWrapper
) : SearchService {

    private val gson = Gson()

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
            val analysisKey = CacheKeyGenerator.analysisKey(query)
            val analysisJson = redis.getOrSet(analysisKey, TTL_SECONDS) {
                val analysis = analyzerService.analyze(query)
                gson.toJson(analysis)
            }
            val analysisResult = gson.fromJson(analysisJson, QueryAnalysisResult::class.java)

            val embeddingKey = CacheKeyGenerator.embeddingKey(query)
            val embeddingJson = redis.getOrSet(embeddingKey, TTL_SECONDS) {
                val req = EmbeddingRequest(
                    mood = analysisResult.mood.orEmpty(),
                    taste = analysisResult.taste.orEmpty(),
                    category = analysisResult.category.orEmpty()
                )
                val result = embeddingService.embed(req)
                gson.toJson(result)
            }
            val embeddingResult = gson.fromJson(embeddingJson, EmbeddingResult::class.java)

            logger.debug("Analysis/Embedding ready – proceeding to search")

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

    suspend fun RedisClientWrapper.getOrSet(
        key: String,
        ttlSeconds: Long,
        fetch: suspend () -> String
    ): String {
        return this.get(key) ?: run {
            val value = fetch()
            this.set(key, value, ttlSeconds)
            value
        }
    }

    private fun determineFieldWeights(analysisResult: QueryAnalysisResult): Map<String, Float> {
        val intent = mapIntent(analysisResult.intent)
        val maxWeights = getMaxWeightsForIntent(intent)

        val baseWeights = mutableMapOf(
            "tasteVector" to 1.0f,
            "categoryVector" to 1.0f,
            "moodVector" to 1.0f
        )

        val category = analysisResult.category
        val mood = analysisResult.mood
        val taste = analysisResult.taste

        // Category
        if (!category.isNullOrBlank() && analysisResult.categoryConfidence != null) {
            val categoryWeight = scaleWeight(analysisResult.categoryConfidence, minWeight = 1.0f, maxWeight = maxWeights["categoryVector"]!!)
            baseWeights["categoryVector"] = categoryWeight
        }

        // Mood
        if (!mood.isNullOrBlank() && analysisResult.moodConfidence != null) {
            val moodWeight = scaleWeight(analysisResult.moodConfidence, minWeight = 1.0f, maxWeight = maxWeights["moodVector"]!!)
            baseWeights["moodVector"] = moodWeight
        }

        // Taste
        if (!taste.isNullOrBlank() && analysisResult.tasteConfidence != null) {
            val tasteWeight = scaleWeight(analysisResult.tasteConfidence, minWeight = 1.0f, maxWeight = maxWeights["tasteVector"]!!)
            baseWeights["tasteVector"] = tasteWeight
        }

        // Normalize weights (sum ≈ 3.0)
        val sumWeights = baseWeights.values.sum()
        val normalizedWeights = baseWeights.mapValues { (_, value) ->
            value * (3.0f / sumWeights)
        }

        logger.debug("Intent = {}, field weights = {}", intent, normalizedWeights)

        return normalizedWeights
    }

    private fun resolveWeight(manualWeight: Float, autoWeight: Float): Float {
        return if (manualWeight != 1.0f) manualWeight else autoWeight
    }

    private fun scaleWeight(confidence: Float?, minWeight: Float, maxWeight: Float): Float {
        if (confidence == null || confidence <= 0f) return minWeight
        if (confidence >= 1f) return maxWeight

        return minWeight + (maxWeight - minWeight) * confidence
    }

    fun getMaxWeightsForIntent(intent: SearchIntent): Map<String, Float> {
        return when (intent) {
            SearchIntent.CATEGORY_FOCUSED -> mapOf(
                "categoryVector" to 3.5f,
                "moodVector" to 2.0f,
                "tasteVector" to 1.5f
            )
            SearchIntent.MOOD_FOCUSED -> mapOf(
                "categoryVector" to 2.5f,
                "moodVector" to 3.0f,
                "tasteVector" to 1.5f
            )
            SearchIntent.TASTE_FOCUSED -> mapOf(
                "categoryVector" to 2.5f,
                "moodVector" to 2.0f,
                "tasteVector" to 2.5f
            )
            SearchIntent.GENERIC -> mapOf(
                "categoryVector" to 3.0f,
                "moodVector" to 2.0f,
                "tasteVector" to 1.5f
            )
        }
    }

    private fun mapIntent(intentStr: String?): SearchIntent {
        return when (intentStr) {
            "CATEGORY_FOCUSED" -> SearchIntent.CATEGORY_FOCUSED
            "MOOD_FOCUSED" -> SearchIntent.MOOD_FOCUSED
            "TASTE_FOCUSED" -> SearchIntent.TASTE_FOCUSED
            else -> SearchIntent.GENERIC
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private const val TTL_SECONDS = 300L
    }
}
