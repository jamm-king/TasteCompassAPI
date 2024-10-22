package com.toy.tasteCompass.test.dto

data class EmbeddingDataResponse(
        val index: String,
        val embedding: List<Float>,
        val `object`: String,
)