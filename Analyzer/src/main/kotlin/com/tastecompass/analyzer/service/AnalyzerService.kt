package com.tastecompass.analyzer.service

import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.domain.entity.Review

interface AnalyzerService {
    fun analyze(review: Review): AnalysisResult
}