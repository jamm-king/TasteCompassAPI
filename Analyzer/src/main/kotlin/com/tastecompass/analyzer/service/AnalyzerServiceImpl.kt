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
        val prompt = PromptTemplate.forReviewAnalysis(review.text)
        val response = client.chat(prompt)

        return try {
            gson.fromJson(response, AnalysisResult::class.java)
        } catch (e: JsonSyntaxException) {
            logger.error("failed to parse: $response")
            AnalysisResult(null, null, null, null)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}