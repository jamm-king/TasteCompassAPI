package com.entity

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class Restaurant(
    var id: Long,
    var name: String? = RestaurantProperty.NAME.defaultValue as String,
    var minPrice: Float? = RestaurantProperty.MIN_PRICE.defaultValue as Float,
    var maxPrice: Float? = RestaurantProperty.MAX_PRICE.defaultValue as Float,
    var reviews: List<String> = RestaurantProperty.REVIEWS.defaultValue as List<String>,
    var mood: String? = RestaurantProperty.MOOD.defaultValue as String,
    var moodVector: List<Float> = RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>
) {

    fun toReadableString(): String {
        return "[$id]$name, price $minPrice ~ $maxPrice , review count : ${reviews.size}, mood: $mood, moodVectorExists : ${moodVector.isNotEmpty()}"
    }


    fun toJsonObject(): JsonObject {
        val jsonObject = JsonObject().apply {
            addProperty("id", id)
            addProperty("name", name ?: RestaurantProperty.NAME.defaultValue as String)
            addProperty("min_price", minPrice ?: RestaurantProperty.MIN_PRICE.defaultValue as Float)
            addProperty("max_price", maxPrice ?: RestaurantProperty.MAX_PRICE.defaultValue as Float)
            add("reviews", JsonArray().apply {
                reviews.forEach { add(it) }
            })
            addProperty("mood", mood ?: "N/A")
            add("mood_vector", JsonArray().apply {
                moodVector.forEach { add(it) }
            })
        }

        return jsonObject
    }

    companion object {
        fun fromJsonObject(jsonObject: JsonObject): Restaurant {
            return Restaurant(
                id = jsonObject.get("id").asLong,
                name = jsonObject.get("name")?.asString ?: RestaurantProperty.NAME.defaultValue as String,
                minPrice = jsonObject.get("min_price")?.asFloat ?: RestaurantProperty.MIN_PRICE.defaultValue as Float,
                maxPrice = jsonObject.get("max_price")?.asFloat ?: RestaurantProperty.MAX_PRICE.defaultValue as Float,
                reviews = jsonObject.getAsJsonArray("reviews")?.let { reviewsToList(it) }
                    ?: RestaurantProperty.REVIEWS.defaultValue as List<String>,
                mood = jsonObject.get("mood")?.asString ?: RestaurantProperty.MOOD.defaultValue as String,
                moodVector = jsonObject.getAsJsonArray("mood_vector")?.let { moodVectorToList(it) }
                    ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
            )
        }

        fun fromMap(map: Map<String, Any>): Restaurant {
            return Restaurant(
                id = map["id"] as Long,
                name = map["name"] as? String ?: RestaurantProperty.NAME.defaultValue as String,
                minPrice = map["min_price"] as? Float ?: RestaurantProperty.MIN_PRICE.defaultValue as Float,
                maxPrice = map["max_price"] as? Float ?: RestaurantProperty.MAX_PRICE.defaultValue as Float,
                reviews = (map["reviews"] as? List<*>)?.filterIsInstance<String>()
                    ?: RestaurantProperty.REVIEWS.defaultValue as List<String>,
                mood = map["mood"] as? String ?: RestaurantProperty.MOOD.defaultValue as String,
                moodVector = (map["mood_vector"] as? List<*>)?.filterIsInstance<Float>()
                    ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>
            )
        }

        private fun moodVectorToList(jsonArray: JsonArray): List<Float> {
            val floatList = mutableListOf<Float>()
            for (i in 0 until jsonArray.size()) {
                val value = jsonArray[i].asFloat
                floatList.add(value)
            }
            return floatList
        }

        private fun reviewsToList(jsonArray: JsonArray): List<String> {
            val reviewList = mutableListOf<String>()
            for (i in 0 until jsonArray.size()) {
                val value = jsonArray[i].asString
                reviewList.add(value)
            }
            return reviewList
        }
    }
}