package com.tastecompass.analyzer.dto

data class QueryAnalysisResult(
    val mood: String?,
    val moodConfidence: Float?,
    val taste: String?,
    val tasteConfidence: Float?,
    val category: String?,
    val categoryConfidence: Float?
)

