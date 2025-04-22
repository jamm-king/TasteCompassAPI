package com.tastecompass.analyzer.service

interface Analyzer {
    suspend fun analyze(text: String): Any
}