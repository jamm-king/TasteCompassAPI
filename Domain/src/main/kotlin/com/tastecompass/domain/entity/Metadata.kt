package com.tastecompass.domain.entity

import com.tastecompass.domain.common.AnalyzeStep
import org.bson.Document

data class Metadata(
    val id: String,
    val status: AnalyzeStep,
    val source: String = RestaurantProperty.SOURCE.defaultValue as String,
    val name: String = RestaurantProperty.NAME.defaultValue as String,
    val category: String = RestaurantProperty.CATEGORY.defaultValue as String,
    val phone: String = RestaurantProperty.PHONE.defaultValue as String,
    val address: String = RestaurantProperty.ADDRESS.defaultValue as String,
    val x: Double = RestaurantProperty.X.defaultValue as Double,
    val y: Double = RestaurantProperty.Y.defaultValue as Double,
    val reviews: List<Review> = RestaurantProperty.REVIEWS.defaultValue as List<Review>,
    val businessDays: String = RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
    val hasWifi: Boolean = RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
    val hasParking: Boolean = RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
    val menus: List<RestaurantMenu> = RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
    val minPrice: Int = RestaurantProperty.MIN_PRICE.defaultValue as Int,
    val maxPrice: Int = RestaurantProperty.MAX_PRICE.defaultValue as Int,
    val mood: List<String> = RestaurantProperty.MOOD.defaultValue as List<String>,
    val taste: List<String> = RestaurantProperty.TASTE.defaultValue as List<String>,
) {
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
    ): Metadata {
        return copy(
            status = status ?: this.status,
            source = source ?: this.source,
            name = name ?: this.name,
            category = category ?: this.category,
            phone = phone ?: this.phone,
            address = address ?: this.address,
            x = x ?: this.x,
            y = y ?: this.y,
            reviews = reviews ?: this.reviews,
            businessDays = businessDays ?: this.businessDays,
            hasWifi = hasWifi ?: this.hasWifi,
            hasParking = hasParking ?: this.hasParking,
            menus = menus ?: this.menus,
            minPrice = minPrice ?: this.minPrice,
            maxPrice = maxPrice ?: this.maxPrice,
            mood = mood ?: this.mood,
            taste = taste ?: this.taste
        )
    }

    fun toDocument(): Document {
        return Document().apply {
            put(RestaurantProperty.ID.key, id)
            put(RestaurantProperty.STATUS.key, status)
            put(RestaurantProperty.SOURCE.key, source)
            put(RestaurantProperty.NAME.key, name)
            put(RestaurantProperty.CATEGORY.key, category)
            put(RestaurantProperty.PHONE.key, phone)
            put(RestaurantProperty.ADDRESS.key, address)
            put(RestaurantProperty.X.key, x)
            put(RestaurantProperty.Y.key, y)
            put(RestaurantProperty.REVIEWS.key, reviews)
            put(RestaurantProperty.BUSINESS_DAYS.key, businessDays)
            put(RestaurantProperty.HAS_WIFI.key, hasWifi)
            put(RestaurantProperty.HAS_PARKING.key, hasParking)
            put(RestaurantProperty.MENUS.key, menus)
            put(RestaurantProperty.MIN_PRICE.key, minPrice)
            put(RestaurantProperty.MAX_PRICE.key, maxPrice)
            put(RestaurantProperty.MOOD.key, mood)
            put(RestaurantProperty.TASTE.key, taste)
        }
    }

    companion object {
        fun fromDocument(document: Document): Metadata {
            val rawReviews = document.getList("reviews", Document::class.java)
            val reviews = rawReviews.map { doc ->
                Review(
                    source  = doc.getString("source"),
                    url     = doc.getString("url"),
                    address = doc.getString("address"),
                    text    = doc.getString("text")
                )
            }
            val rawMenus = document.getList("menus", Document::class.java)
            val menus = rawMenus.map { doc ->
                RestaurantMenu(
                    name     = doc.getString("name"),
                    price    = doc.getInteger("price"),
                )
            }
            val mood  = document.getList("mood", String::class.java)
            val taste = document.getList("taste", String::class.java)

            return Metadata(
                id = document["id"] as String,
                status = AnalyzeStep.valueOf(document.getString("status")),
                source = document["source"] as? String ?: RestaurantProperty.SOURCE.defaultValue as String,
                name = document["name"] as? String ?: RestaurantProperty.NAME.defaultValue as String,
                category = document["category"] as? String ?: RestaurantProperty.CATEGORY.defaultValue as String,
                phone = document["phone"] as? String ?: RestaurantProperty.PHONE.defaultValue as String,
                address = document["address"] as? String ?: RestaurantProperty.ADDRESS.defaultValue as String,
                x = document["x"] as? Double ?: RestaurantProperty.X.defaultValue as Double,
                y = document["y"] as? Double ?: RestaurantProperty.Y.defaultValue as Double,
                reviews = reviews,
                businessDays = document["businessDays"] as? String ?: RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
                hasWifi = document["hasWifi"] as? Boolean ?: RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
                hasParking = document["hasParking"] as? Boolean ?: RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
                menus = menus,
                minPrice = document["minPrice"] as? Int ?: RestaurantProperty.MIN_PRICE.defaultValue as Int,
                maxPrice = document["maxPrice"] as? Int ?: RestaurantProperty.MAX_PRICE.defaultValue as Int,
                mood = mood,
                taste = taste
            )
        }
    }
}

