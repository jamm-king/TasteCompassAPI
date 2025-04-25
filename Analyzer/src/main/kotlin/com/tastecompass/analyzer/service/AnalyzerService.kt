package com.tastecompass.analyzer.service

import com.tastecompass.analyzer.dto.AnalysisResult

interface AnalyzerService {
    fun analyze(text: String): AnalysisResult
}