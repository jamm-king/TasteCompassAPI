package com.tastecompass.domain.entity

import com.tastecompass.domain.common.Constants
import com.tastecompass.domain.common.AnalyzeStep

enum class RestaurantProperty(val key: String, val defaultValue: Any) {
    ID("id", "N/A"),
    STATUS("status", AnalyzeStep.PREPARED),
    SOURCE("source", "N/A"),
    NAME("name", "N/A"),
    CATEGORY("category", "N/A"),
    CATEGORY_VECTOR("categoryVector", List(Constants.EMBEDDING_SIZE) { 0.0f }),
    PHONE("phone", "N/A"),
    ADDRESS("address", "N/A"),
    X("x", 0.0),
    Y("y", 0.0),
    REVIEWS("reviews", emptyList<Review>()),
    BUSINESS_DAYS("businessDays", "N/A"),
    HAS_WIFI("hasWifi", false),
    HAS_PARKING("hasParking", false),
    MENUS("menus", emptyList<Any>()),
    MIN_PRICE("minPrice", 0),
    MAX_PRICE("maxPrice", 0),
    MOOD("mood", emptyList<String>()),
    MOOD_VECTOR("moodVector", List(Constants.EMBEDDING_SIZE) { 0.0f }),
    TASTE("taste", emptyList<String>()),
    TASTE_VECTOR("tasteVector", List(Constants.EMBEDDING_SIZE) { 0.0f });
}
