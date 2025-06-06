package com.tastecompass.analyzer.service

import com.tastecompass.analyzer.dto.FullAnalysisResult
import com.tastecompass.analyzer.dto.QueryAnalysisResult
import com.tastecompass.domain.entity.Review

interface AnalyzerService {
    suspend fun analyze(review: Review): FullAnalysisResult
    suspend fun analyze(query: String): QueryAnalysisResult
}