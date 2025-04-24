package com.tastecompass.analyzer.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tastecompass.analyzer.client.OpenAIChatter
import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.analyzer.prompt.PromptTemplate
import org.springframework.stereotype.Service

@Service
class ReviewAnalyzer(
    private val openaiClient: OpenAIChatter
): Analyzer {
    private val gson = Gson()

    override suspend fun analyze(review: String): AnalysisResult {
        val prompt = PromptTemplate.forReviewAnalysis(review)
        val response = openaiClient.chat(prompt)

        return try {
            gson.fromJson(response, AnalysisResult::class.java)
        } catch (e: JsonSyntaxException) {
            println("failed to parse: $response")
            AnalysisResult(null, null, null, null)
        }
    }
}