package com.tastecompass.service

import com.tastecompass.domain.entity.Restaurant

interface SearchService {
    suspend fun search(
        query: String,
        topK: Int,
        tasteWeight: Float,
        categoryWeight: Float,
        moodWeight: Float
    ): List<Restaurant>
}
