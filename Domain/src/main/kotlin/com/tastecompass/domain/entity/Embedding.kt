package com.tastecompass.domain.entity

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class Embedding(
    val id: String,
    val category: String = RestaurantProperty.CATEGORY.defaultValue as String,
    val address: String = RestaurantProperty.ADDRESS.defaultValue as String,
    val x: Double = RestaurantProperty.X.defaultValue as Double,
    val y: Double = RestaurantProperty.Y.defaultValue as Double,
    val businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
    val hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
    val hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
    val minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
    val maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
    val moodVector: List<Float> = RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
    val tasteVector: List<Float> = RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>,
    val categoryVector: List<Float> = RestaurantProperty.CATEGORY_VECTOR.defaultValue as List<Float>
) {
    constructor(
        metadata: Metadata, moodVector: List<Float>?, tasteVector: List<Float>?, categoryVector: List<Float>?
    ) : this(
        id = metadata.id, category = metadata.category, address = metadata.address,
        x = metadata.x, y = metadata.y, businessDays = metadata.businessDays,
        hasWifi = metadata.hasWifi, hasParking = metadata.hasParking,
        minPrice = metadata.minPrice, maxPrice = metadata.maxPrice,
        moodVector = moodVector ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
        tasteVector = tasteVector ?: RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>,
        categoryVector = categoryVector ?: RestaurantProperty.CATEGORY_VECTOR.defaultValue as List<Float>
    )

    fun update(
        category: String? = null,
        address: String? = null,
        x: Double? = null,
        y: Double? = null,
        businessDays: String? = null,
        hasWifi: Boolean? = null,
        hasParking: Boolean? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        moodVector: List<Float>? = null,
        tasteVector: List<Float>? = null,
        categoryVector: List<Float>? = null
    ): Embedding {
        return copy(
            category = category ?: this.category,
            address = address ?: this.address,
            x = x ?: this.x,
            y = y ?: this.y,
            businessDays = businessDays ?: this.businessDays,
            hasWifi = hasWifi ?: this.hasWifi,
            hasParking = hasParking ?: this.hasParking,
            minPrice = minPrice ?: this.minPrice,
            maxPrice = maxPrice ?: this.maxPrice,
            moodVector = moodVector ?: this.moodVector,
            tasteVector = tasteVector ?: this.tasteVector,
            categoryVector = categoryVector ?: this.categoryVector
        )
    }

    fun toJsonObject(): JsonObject {
        return JsonObject().apply {
            addProperty(RestaurantProperty.ID.key, id)
            addProperty(RestaurantProperty.CATEGORY.key, category)
            addProperty(RestaurantProperty.ADDRESS.key, address)
            addProperty(RestaurantProperty.X.key, x)
            addProperty(RestaurantProperty.Y.key, y)
            addProperty(RestaurantProperty.BUSINESS_DAYS.key, businessDays)
            addProperty(RestaurantProperty.HAS_WIFI.key, hasWifi)
            addProperty(RestaurantProperty.HAS_PARKING.key, hasParking)
            addProperty(RestaurantProperty.MIN_PRICE.key, minPrice)
            addProperty(RestaurantProperty.MAX_PRICE.key, maxPrice)
            add(RestaurantProperty.MOOD_VECTOR.key, JsonArray().apply {
                moodVector.forEach { add(it) }
            })
            add(RestaurantProperty.TASTE_VECTOR.key, JsonArray().apply {
                tasteVector.forEach { add(it) }
            })
            add(RestaurantProperty.CATEGORY_VECTOR.key, JsonArray().apply {
                categoryVector.forEach { add(it) }
            })
        }
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Embedding {
            return Embedding(
                id = map[RestaurantProperty.ID.key] as String,
                category = map[RestaurantProperty.CATEGORY.key] as? String ?: RestaurantProperty.CATEGORY.defaultValue as String,
                address = map[RestaurantProperty.ADDRESS.key] as? String ?: RestaurantProperty.ADDRESS.defaultValue as String,
                x = map[RestaurantProperty.X.key] as? Double ?: RestaurantProperty.X.defaultValue as Double,
                y = map[RestaurantProperty.Y.key] as? Double ?: RestaurantProperty.Y.defaultValue as Double,
                businessDays = map[RestaurantProperty.BUSINESS_DAYS.key] as? String ?: RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
                hasWifi = map[RestaurantProperty.HAS_WIFI.key] as? Boolean ?: RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
                hasParking = map[RestaurantProperty.HAS_PARKING.key] as? Boolean ?: RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
                minPrice = map[RestaurantProperty.MIN_PRICE.key] as? Int ?: RestaurantProperty.MIN_PRICE.defaultValue as Int,
                maxPrice = map[RestaurantProperty.MAX_PRICE.key] as? Int ?: RestaurantProperty.MAX_PRICE.defaultValue as Int,
                moodVector = map[RestaurantProperty.MOOD_VECTOR.key] as? List<Float> ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
                tasteVector = map[RestaurantProperty.TASTE_VECTOR.key] as? List<Float> ?: RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>,
                categoryVector = map[RestaurantProperty.CATEGORY_VECTOR.key] as? List<Float> ?: RestaurantProperty.CATEGORY_VECTOR.defaultValue as List<Float>
            )
        }
    }
}
