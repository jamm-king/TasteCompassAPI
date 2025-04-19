package com.tastecompass.analyzer

interface Analyzer {
    suspend fun analyze(text: String): String
}