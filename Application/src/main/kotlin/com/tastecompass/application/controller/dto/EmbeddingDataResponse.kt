package com.tastecompass.application.controller.dto

data class EmbeddingDataResponse(
        val index: String,
        val embedding: List<Float>,
        val `object`: String,
)