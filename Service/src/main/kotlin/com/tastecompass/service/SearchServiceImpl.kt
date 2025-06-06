package com.tastecompass.service

import com.tastecompass.domain.entity.Restaurant
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl: SearchService {
    override suspend fun search(query: String): List<Restaurant> {
        TODO("Not yet implemented")
    }
}