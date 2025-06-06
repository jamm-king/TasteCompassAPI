package com.tastecompass.embedding.service

import com.tastecompass.embedding.dto.EmbeddingRequest
import com.tastecompass.embedding.dto.EmbeddingResult

interface EmbeddingService {
    suspend fun embed(embeddingReq: EmbeddingRequest): EmbeddingResult
}