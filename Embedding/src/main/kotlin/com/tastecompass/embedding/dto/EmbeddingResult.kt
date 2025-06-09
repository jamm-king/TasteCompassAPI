package com.tastecompass.embedding.dto

data class EmbeddingResult(
    val moodVector: List<Float>,
    val tasteVector: List<Float>,
    val categoryVector: List<Float>
)
