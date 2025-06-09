package com.tastecompass.domain.entity

import com.tastecompass.domain.common.AnalyzeStep
import java.util.logging.Logger

data class Restaurant(
    val metadata: Metadata,
    val embedding: Embedding?,
) {
    val id: String
        get() = metadata.id

    val status: AnalyzeStep
        get() = metadata.status

    val name: String
        get() = metadata.name

    val category: String
        get() = metadata.category

    val phone: String
        get() = metadata.phone

    val address: String
        get() = metadata.address

    val x: Double
        get() = metadata.x

    val y: Double
        get() = metadata.y

    val reviews: List<Review>
        get() = metadata.reviews

    val businessDays: String
        get() = metadata.businessDays

    val hasWifi: Boolean
        get() = metadata.hasWifi

    val hasParking: Boolean
        get() = metadata.hasParking

    val menus: List<RestaurantMenu>
        get() = metadata.menus

    val minPrice: Int
        get() = metadata.minPrice

    val maxPrice: Int
        get() = metadata.maxPrice

    val mood: List<String>
        get() = metadata.mood

    val taste: List<String>
        get() = metadata.taste

    val moodVector: List<Float>
        get() = embedding?.moodVector ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>

    val tasteVector: List<Float>
        get() = embedding?.tasteVector ?: RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>

    val categoryVector: List<Float>
        get() = embedding?.categoryVector ?: RestaurantProperty.CATEGORY_VECTOR.defaultValue as List<Float>

    fun update(
        status: AnalyzeStep? = null,
        source: String? = null,
        name: String? = null,
        category: String? = null,
        phone: String? = null,
        address: String? = null,
        x: Double? = null,
        y: Double? = null,
        reviews: List<Review>? = null,
        businessDays: String? = null,
        hasWifi: Boolean? = null,
        hasParking: Boolean? = null,
        menus: List<RestaurantMenu>? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        mood: List<String>? = null,
        taste: List<String>? = null,
        moodVector: List<Float>? = null,
        tasteVector: List<Float>? = null,
        categoryVector: List<Float>? = null
    ) : Restaurant {
        var newMetadata = metadata.update(
            status = status, source = source, name = name, category = category,
            phone = phone, address = address, x = x, y = y, reviews = reviews,
            businessDays = businessDays, hasWifi = hasWifi, hasParking = hasParking,
            menus = menus, minPrice = minPrice, maxPrice = maxPrice, mood = mood, taste = taste
        )
        val newEmbedding = embedding?.update(
            category = category, address = address, x = x, y = y, businessDays = businessDays,
            hasWifi = hasWifi, hasParking = hasParking, minPrice = minPrice, maxPrice = maxPrice,
            moodVector =  moodVector, tasteVector = tasteVector, categoryVector = categoryVector
        ) ?: if(moodVector != null || tasteVector != null || categoryVector != null) {
            newMetadata = newMetadata.update(status = AnalyzeStep.EMBEDDED)
            Embedding(metadata = metadata, moodVector = moodVector, tasteVector = tasteVector, categoryVector = categoryVector)
        }
        else null

        return copy(metadata = newMetadata, embedding = newEmbedding)
    }

    fun addReview(
        newReview: Review
    ): Restaurant {
        val updatedReviews = mutableListOf<Review>()
        updatedReviews.addAll(reviews)
        updatedReviews.add(newReview)

        return update(reviews = updatedReviews)
    }

    fun addTaste(
        newTaste: String
    ): Restaurant {
        val updatedTaste = mutableListOf<String>()
        updatedTaste.addAll(taste)
        updatedTaste.add(newTaste)

        return update(taste = updatedTaste)
    }

    fun addMood(
        newMood: String
    ): Restaurant {
        val updatedMood = mutableListOf<String>()
        updatedMood.addAll(mood)
        updatedMood.add(newMood)

        return update(mood = updatedMood)
    }

    fun updateTasteVector(
        newVector: List<Float>
    ): Restaurant {
        val updatedVector = weightedAverageVector(tasteVector, taste.size, newVector)

        return update(tasteVector = updatedVector)
    }

    fun updateMoodVector(
        newVector: List<Float>
    ): Restaurant {
        val updatedVector = weightedAverageVector(moodVector, mood.size, newVector)

        return update(moodVector = updatedVector)
    }

    private fun weightedAverageVector(
        old: List<Float>,
        oldCount: Int,
        new: List<Float>
    ): List<Float> {
        return old.zip(new).map { (o, n) ->
            (o * oldCount + n) / (oldCount + 1)
        }
    }

    override fun toString(): String {
        return "Restaurant(id=$id, name=${metadata.name}, status=$status)"
    }

    private val logger = Logger.getLogger(TAG)

    companion object {
        private const val TAG = "Restaurant"

        fun create(metadata: Metadata, embedding: Embedding?): Restaurant {
            return Restaurant(
                metadata = metadata,
                embedding = embedding,
            )
        }

        fun create(
            id: String,
            status: AnalyzeStep = RestaurantProperty.STATUS.defaultValue as AnalyzeStep,
            source: String = RestaurantProperty.SOURCE.defaultValue as String,
            name: String = RestaurantProperty.NAME.defaultValue as String,
            category: String = RestaurantProperty.CATEGORY.defaultValue as String,
            phone: String = RestaurantProperty.PHONE.defaultValue as String,
            address: String = RestaurantProperty.ADDRESS.defaultValue as String,
            x: Double = RestaurantProperty.X.defaultValue as Double,
            y: Double = RestaurantProperty.Y.defaultValue as Double,
            reviews: List<Review> = RestaurantProperty.REVIEWS.defaultValue as List<Review>,
            businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
            hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
            hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
            menus: List<RestaurantMenu> = RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
            minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
            maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
            mood: List<String> = RestaurantProperty.MOOD.defaultValue as List<String>,
            taste: List<String> = RestaurantProperty.TASTE.defaultValue as List<String>
        ): Restaurant {
            val metadata = Metadata(
                id = id, status = status, source = source, name = name, category = category,
                phone = phone, address = address, x = x, y = y, reviews = reviews,
                businessDays = businessDays, hasWifi = hasWifi, hasParking = hasParking,
                menus = menus, minPrice = minPrice, maxPrice = maxPrice, mood = mood, taste = taste
            )

            return Restaurant(metadata = metadata, embedding = null)
        }

        fun create(
            id: String,
            status: AnalyzeStep = RestaurantProperty.STATUS.defaultValue as AnalyzeStep,
            source: String = RestaurantProperty.SOURCE.defaultValue as String,
            name: String = RestaurantProperty.NAME.defaultValue as String,
            category: String = RestaurantProperty.CATEGORY.defaultValue as String,
            phone: String = RestaurantProperty.PHONE.defaultValue as String,
            address: String = RestaurantProperty.ADDRESS.defaultValue as String,
            x: Double = RestaurantProperty.X.defaultValue as Double,
            y: Double = RestaurantProperty.Y.defaultValue as Double,
            reviews: List<Review> = RestaurantProperty.REVIEWS.defaultValue as List<Review>,
            businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
            hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
            hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
            menus: List<RestaurantMenu> = RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
            minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
            maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
            mood: List<String> = RestaurantProperty.MOOD.defaultValue as List<String>,
            taste: List<String> = RestaurantProperty.TASTE.defaultValue as List<String>,
            moodVector: List<Float> = RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
            tasteVector: List<Float> = RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>,
            categoryVector: List<Float> = RestaurantProperty.CATEGORY_VECTOR.defaultValue as List<Float>
        ): Restaurant {
            val metadata = Metadata(
                id = id, status = status, source = source, name = name, category = category,
                phone = phone, address = address, x = x, y = y, reviews = reviews,
                businessDays = businessDays, hasWifi = hasWifi, hasParking = hasParking,
                menus = menus, minPrice = minPrice, maxPrice = maxPrice, mood = mood, taste = taste
            )
            val embedding = Embedding(
                metadata = metadata,
                moodVector = moodVector,
                tasteVector = tasteVector,
                categoryVector = categoryVector
            )

            return Restaurant(metadata = metadata, embedding = embedding)
        }
    }
}