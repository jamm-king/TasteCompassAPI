package com.tastecompass.analyzer.dto

data class AnalysisResult(
    val name: String? = null,
    val category: String? = null,
    val menus: List<Any>? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val taste: String? = null,
    val mood: String? = null,
    val hasWifi: Boolean? = null,
    val hasParking: Boolean? = null,
    val businessDays: String? = null
)
