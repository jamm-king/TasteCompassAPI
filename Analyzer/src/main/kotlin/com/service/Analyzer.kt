package com.service

interface Analyzer {
    suspend fun analyze(text: String): String
}