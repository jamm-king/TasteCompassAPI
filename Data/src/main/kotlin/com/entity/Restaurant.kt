package com.entity

import kotlinx.coroutines.Job
import org.json.JSONArray
import org.json.JSONObject

data class Restaurant(
    var id: Long,
    var name: String? = null,
    var minPrice: Float? = null,
    var maxPrice: Float? = null,
    var reviews:List<String> = mutableListOf(),
    var mood: String? = null,
    var moodVector: List<Float>? = null) {

    fun toReadableString(): String {
        return "[$id]$name, price $minPrice ~ $maxPrice , review count : ${reviews.size}, mood: $mood, moodVectorExists : ${moodVector != null}"
    }

    fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject().apply {
            put("collectionName", "Restaurant")
            put("data", JSONArray(listOf(
                JSONObject().apply {
                    put("mood_vector", JSONArray(moodVector ?: List<Float>(1536) {0.0f}))
                    put("id", id)
                    put("name", name?: "N/A")
                    put("min_price", minPrice?: -1f)
                    put("max_price", maxPrice?: -1f)
                    put("reviews", JSONArray()) // Need to convert
                    put("mood", mood?: "N/A")
                }
            )))
        }
        return jsonObject
    }

    companion object {
        fun fromJsonObject(jsonObject: JSONObject): Restaurant {
            return Restaurant(
                id = jsonObject.getLong("id"),
                maxPrice = jsonObject.getDouble("max_price").takeIf { it >= 0 }?.toFloat(),
                minPrice = jsonObject.getDouble("min_price").takeIf { it >= 0 }?.toFloat(),
                moodVector = moodVectorToList(jsonObject.getJSONArray("mood_vector")),
                name = jsonObject.getString("name").takeIf {it != "N/A"},
                reviews = kotlin.runCatching {
                    reviewsToList(jsonObject.getJSONObject("reviews").getJSONObject("Data").getJSONObject("StringData").getJSONArray("data"))
                }.getOrDefault(listOf())
            )
        }

        private fun moodVectorToList(jsonArray: JSONArray): List<Float> {
            val floatList = mutableListOf<Float>()
            for (i in 0 until jsonArray.length()) {
                val value = jsonArray.getDouble(i).toFloat()
                floatList.add(value)
            }
            return floatList
        }

        private fun reviewsToList(jsonArray: JSONArray): List<String> {
            val reviewList = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val value = jsonArray.getString(i)
                reviewList.add(value)
            }
            return reviewList
        }

    }
}
