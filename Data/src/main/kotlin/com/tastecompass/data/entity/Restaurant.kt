package com.tastecompass.data.entity

import com.tastecompass.data.common.AnalyzeStep
import java.util.logging.Logger

data class Restaurant(
    val metadata: RestaurantMetadata,
    val embedding: RestaurantEmbedding?,
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

    val reviews: List<String>
        get() = metadata.reviews

    val businessDays: String
        get() = metadata.businessDays

    val url: String
        get() = metadata.url

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

    val mood: String
        get() = metadata.mood

    val taste: String
        get() = metadata.taste

    val moodVector: List<Float>
        get() = embedding?.moodVector ?: RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>

    val tasteVector: List<Float>
        get() = embedding?.tasteVector ?: RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>

    fun update(
        status: AnalyzeStep? = null,
        source: String? = null,
        name: String? = null,
        category: String? = null,
        phone: String? = null,
        address: String? = null,
        x: Double? = null,
        y: Double? = null,
        reviews: List<String>? = null,
        businessDays: String? = null,
        url: String? = null,
        hasWifi: Boolean? = null,
        hasParking: Boolean? = null,
        menus: List<RestaurantMenu>? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        mood: String? = null,
        taste: String? = null,
        moodVector: List<Float>? = null,
        tasteVector: List<Float>? = null
    ) : Restaurant {
        var newMetadata = metadata.update(
            status = status, source = source, name = name, category = category,
            phone = phone, address = address, x = x, y = y, reviews = reviews,
            businessDays = businessDays, url = url, hasWifi = hasWifi, hasParking = hasParking,
            menus = menus, minPrice = minPrice, maxPrice = maxPrice, mood = mood, taste = taste
        )
        val newEmbedding = embedding?.update(
            category = category, address = address, x = x, y = y, businessDays = businessDays,
            hasWifi = hasWifi, hasParking = hasParking, minPrice = minPrice, maxPrice = maxPrice,
            moodVector =  moodVector, tasteVector = tasteVector
        ) ?: if(moodVector != null || tasteVector != null) {
            newMetadata = newMetadata.update(status = AnalyzeStep.EMBEDDED)
            RestaurantEmbedding(metadata = metadata, moodVector = moodVector, tasteVector = tasteVector)
        }
        else null

        return copy(metadata = newMetadata, embedding = newEmbedding)
    }

    fun toReadableString(): String {
        return "Restaurant(id=$id, name=${metadata.name}, status=$status)"
    }

    private val logger = Logger.getLogger(TAG)

    companion object {
        private const val TAG = "Restaurant"

        fun create(metadata: RestaurantMetadata, embedding: RestaurantEmbedding?): Restaurant {
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
            reviews: List<String> = RestaurantProperty.REVIEWS.defaultValue as List<String>,
            businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
            url: String = RestaurantProperty.URL.defaultValue as String,
            hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
            hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
            menus: List<RestaurantMenu> = RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
            minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
            maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
            mood: String = RestaurantProperty.MOOD.defaultValue as String,
            taste: String = RestaurantProperty.TASTE.defaultValue as String
        ): Restaurant {
            val metadata = RestaurantMetadata(
                id = id, status = status, source = source, name = name, category = category,
                phone = phone, address = address, x = x, y = y, reviews = reviews,
                businessDays = businessDays, url = url, hasWifi = hasWifi, hasParking = hasParking,
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
            reviews: List<String> = RestaurantProperty.REVIEWS.defaultValue as List<String>,
            businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
            url: String = RestaurantProperty.URL.defaultValue as String,
            hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
            hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
            menus: List<RestaurantMenu> = RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
            minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
            maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
            mood: String = RestaurantProperty.MOOD.defaultValue as String,
            taste: String = RestaurantProperty.TASTE.defaultValue as String,
            moodVector: List<Float> = RestaurantProperty.MOOD_VECTOR.defaultValue as List<Float>,
            tasteVector: List<Float> = RestaurantProperty.TASTE_VECTOR.defaultValue as List<Float>
        ): Restaurant {
            val metadata = RestaurantMetadata(
                id = id, status = status, source = source, name = name, category = category,
                phone = phone, address = address, x = x, y = y, reviews = reviews,
                businessDays = businessDays, url = url, hasWifi = hasWifi, hasParking = hasParking,
                menus = menus, minPrice = minPrice, maxPrice = maxPrice, mood = mood, taste = taste
            )
            val embedding = RestaurantEmbedding(
                metadata = metadata,
                moodVector = moodVector,
                tasteVector = tasteVector
            )

            return Restaurant(metadata = metadata, embedding = embedding)
        }
    }
}