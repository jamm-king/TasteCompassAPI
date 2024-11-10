package com.entity

import com.common.Constants

enum class RestaurantProperty(
    val key: String,
    val defaultValue: Any
) {
    ID("id", "-1"),
    NAME("name", "N/A"),
    MIN_PRICE("min_price", -1f),
    MAX_PRICE("max_price", -1f),
    REVIEWS("reviews", listOf<String>()),
    MOOD("mood", "N/A"),
    MOOD_VECTOR("mood_vector",  List(Constants.EMBEDDING_SIZE) { 0.0f });

    companion object {
        fun getKeys(): List<String> {
            return entries.map { it.key }
        }
    }
}