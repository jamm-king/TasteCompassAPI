package com.tastecompass.embedding.dto

data class EmbeddingResult(
    val moodVector: List<Double>,
    val tasteVector: List<Double>
)
