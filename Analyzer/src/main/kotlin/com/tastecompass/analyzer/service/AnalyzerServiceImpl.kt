package com.tastecompass.analyzer.service

import com.google.gson.Gson
import com.tastecompass.analyzer.dto.FullAnalysisResult
import com.tastecompass.analyzer.dto.OpenAIAnalysisResult
import com.tastecompass.analyzer.prompt.PromptTemplate
import com.tastecompass.domain.entity.Review
import com.tastecompass.kakao.client.KakaoMapClient
import com.tastecompass.openai.client.OpenAIClientWrapper
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AnalyzerServiceImpl(
    private val openaiClient: OpenAIClientWrapper,
    private val kakaomapClient: KakaoMapClient
): AnalyzerService {
    private val gson = Gson()

    override suspend fun analyze(
        review: Review
    ): FullAnalysisResult = coroutineScope {
        logger.debug("Starting analysis for review from {}", review.source)
        logger.debug("Review text:\n{}", review.text)

        val rawJson = try {
            val prompt = PromptTemplate.forReviewAnalysis(review.text)
            logger.debug("Generated prompt:\n{}", prompt)

            openaiClient.chat(prompt)
        } catch (e: Exception) {
            logger.error("Failed to receive response from OpenAI: {}", e.message)
            throw e
        }

        val openaiAnalysisResult = try {
            gson.fromJson(rawJson, OpenAIAnalysisResult::class.java).also { it.validate(rawJson) }
        } catch (e: Exception) {
            logger.error("Failed to parse analysis result: {}", e.message)
            throw e
        }

        val geo = kakaomapClient.geocode(openaiAnalysisResult.address)
        logger.debug("Geocoded address='{}', x={}, y={}", geo.normalizedAddress, geo.x, geo.y)

        FullAnalysisResult(
            name = openaiAnalysisResult.name,
            category = openaiAnalysisResult.category,
            phone = openaiAnalysisResult.phone,
            address = geo.normalizedAddress,
            x = geo.x,
            y = geo.y,
            businessDays = openaiAnalysisResult.businessDays,
            hasWifi = openaiAnalysisResult.hasWifi,
            hasParking = openaiAnalysisResult.hasParking,
            menus = openaiAnalysisResult.menus,
            minPrice = openaiAnalysisResult.minPrice,
            maxPrice = openaiAnalysisResult.maxPrice,
            mood = openaiAnalysisResult.mood,
            taste = openaiAnalysisResult.taste
        )
    }

    fun OpenAIAnalysisResult.validate(rawJson: String) {
        if (name.isBlank() || address.isBlank() || taste.isBlank() || mood.isBlank()) {
            throw IllegalArgumentException("Missing required fields in analysis result: $rawJson")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}