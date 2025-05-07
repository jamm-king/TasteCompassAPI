package com.tastecompass.analyzer.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.analyzer.prompt.PromptTemplate
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Review
import com.tastecompass.openai.client.OpenAIClientWrapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AnalyzerServiceImpl(
    private val client: OpenAIClientWrapper
): AnalyzerService {
    private val gson = Gson()

    override fun analyze(review: Review): AnalysisResult {
        logger.debug("Starting analysis for review from ${review.source}")
        logger.debug("Review text:\n{}", review.text)

        val response = try {
            val prompt = PromptTemplate.forReviewAnalysis(review.text)
            logger.debug("Generated prompt:\n$prompt")

            val resp = client.chat(prompt)
            logger.debug("Received response from OpenAI:\n$resp")

            resp
        } catch(e: Exception) {
            logger.error("Failed to receive response from OpenAI: ${e.message}")
            throw e
        }

        return try {
            val analysisResult = gson.fromJson(response, AnalysisResult::class.java)

            analysisResult.validate(response)

            logger.debug("Successfully parsed analysis result for review from ${review.source}")
            logger.debug("Analysis result:\n{}", analysisResult)

            analysisResult
        } catch (e: Exception) {
            logger.error("Failed to parse analysis result: ${e.message}")
            throw e
        }
    }

    fun AnalysisResult.validate(rawJson: String) {
        if (name.isBlank() || address.isBlank() || taste.isBlank() || mood.isBlank()) {
            throw IllegalArgumentException("Missing required fields in analysis result: $rawJson")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}