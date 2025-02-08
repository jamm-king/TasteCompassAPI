package com.tastecompass.data.repository.mongo

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.data.entity.RestaurantMetadata
import org.bson.Document
import org.springframework.stereotype.Repository

@Repository
class RestaurantMetadataRepository(
    private val mongoClient: MongoClient
): MongoRepository<RestaurantMetadata> {

    private val database = mongoClient.getDatabase(DATABASE_NAME)
    private val collection = database.getCollection<Document>(COLLECTION_NAME)

    override fun insert(entityList: List<RestaurantMetadata>) {
        val docs = entityList.map { entity ->
            entity.toDocument()
        }

        val insertResult = collection.insertMany(docs)
    }

    override fun update(entityList: List<RestaurantMetadata>) {
        val updateResults = mutableListOf<UpdateResult>()
        entityList.forEach { entity ->
            val filter = eq(RestaurantMetadata::id.name, entity.id)
            val update = combine(
                set(RestaurantMetadata::status.name, entity.status),
                set(RestaurantMetadata::source.name, entity.source),
                set(RestaurantMetadata::name.name, entity.name),
                set(RestaurantMetadata::category.name, entity.category),
                set(RestaurantMetadata::phone.name, entity.phone),
                set(RestaurantMetadata::address.name, entity.address),
                set(RestaurantMetadata::x.name, entity.x),
                set(RestaurantMetadata::y.name, entity.y),
                set(RestaurantMetadata::reviews.name, entity.reviews),
                set(RestaurantMetadata::businessDays.name, entity.businessDays),
                set(RestaurantMetadata::url.name, entity.url),
                set(RestaurantMetadata::hasWifi.name, entity.hasWifi),
                set(RestaurantMetadata::hasParking.name, entity.hasParking),
                set(RestaurantMetadata::menus.name, entity.menus),
                set(RestaurantMetadata::minPrice.name, entity.minPrice),
                set(RestaurantMetadata::maxPrice.name, entity.maxPrice),
                set(RestaurantMetadata::mood.name, entity.mood),
                set(RestaurantMetadata::taste.name, entity.taste)
            )

            val result = collection.updateOne(filter, update)
            updateResults.add(result)
        }
    }

    override fun delete(idList: List<String>) {
        val filter = `in`(RestaurantMetadata::id.name, idList)

        val deleteResult = collection.deleteMany(filter)
    }

    override fun get(idList: List<String>): List<RestaurantMetadata> {
        val filter = `in`(RestaurantMetadata::id.name, idList)

        val getResults = collection.find(filter)
        val entityList = mutableListOf<RestaurantMetadata>()
        getResults.forEach { getResult ->
            entityList.add(RestaurantMetadata.fromDocument(getResult))
        }

        return entityList
    }

    override fun getAll(): List<RestaurantMetadata> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "RestaurantMetadataRepository"
        private const val DATABASE_NAME = "TasteCompass"
        private const val COLLECTION_NAME = "Restaurant"
    }
}