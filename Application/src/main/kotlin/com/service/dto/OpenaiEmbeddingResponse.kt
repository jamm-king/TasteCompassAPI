package com.service.dto

data class OpenaiEmbeddingResponse(
        val index: Long,
        val data: List<EmbeddingDataResponse>,
        val model: String,
        val usage: Map<String, Int>
)