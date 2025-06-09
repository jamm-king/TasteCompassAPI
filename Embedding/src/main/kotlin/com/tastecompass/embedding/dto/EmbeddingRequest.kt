package com.tastecompass.embedding.dto

data class EmbeddingRequest(
    val mood: String = "N/A",
    val taste: String = "N/A",
    val category: String = "N/A"
)
