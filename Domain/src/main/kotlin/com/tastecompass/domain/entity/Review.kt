package com.tastecompass.domain.entity

data class Review(
    val source: String,
    val url: String,
    val text: String,
    val x: Double = 0.0,
    val y: Double = 0.0
)