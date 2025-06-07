package com.tastecompass.service

import com.tastecompass.domain.entity.Restaurant

interface SearchService {
    suspend fun search(query: String, topK: Int): List<Restaurant>
}