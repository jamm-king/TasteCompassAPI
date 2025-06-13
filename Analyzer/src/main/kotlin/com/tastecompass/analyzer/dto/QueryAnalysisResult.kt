package com.tastecompass.analyzer.dto

data class QueryAnalysisResult(
    val taste: String?,
    val tasteConfidence: Float?,
    val mood: String?,
    val moodConfidence: Float?,
    val category: String?,
    val categoryConfidence: Float?,
    val intent: String?
)