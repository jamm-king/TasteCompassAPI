package com.tastecompass.analyzer.dto

import com.tastecompass.domain.entity.RestaurantMenu

data class AnalysisResult(
    val name: String,
    val category: String? = null,
    val phone: String? = null,
    val address: String,
    val businessDays: String? = null,
    val hasWifi: Boolean? = null,
    val hasParking: Boolean? = null,
    val menus: List<RestaurantMenu>? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val mood: String,
    val taste: String,
)
