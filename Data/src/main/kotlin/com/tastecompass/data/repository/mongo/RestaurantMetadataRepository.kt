package com.tastecompass.data.repository.mongo

import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoClientException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.domain.entity.RestaurantMetadata
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import kotlinx.coroutines.*
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class RestaurantMetadataRepository(
    private val mongoClient: MongoClient
): MongoRepository<RestaurantMetadata> {

    private val database = mongoClient.getDatabase(DATABASE_NAME)
    private val collection = database.getCollection<Document>(COLLECTION_NAME)

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun insert(
        entity: RestaurantMetadata
    ): Unit = coroutineScope {
        val document = entity.toDocument()

        val job = launch {
            try {
                collection.insertOne(document)
                logger.debug("Inserted restaurant metadata ${entity.id}")
            } catch (e: MongoWriteException) {
                if(e.error.category.name == "DUPLICATE_KEY") {
                    logger.error("Failed to insert restaurant metadata: ${e.message}")
                    throw InvalidRequestException.duplicateKey(entity)
                } else {
                    throw e
                }
            } catch(e: Exception) {
                logger.error("Unexpected error occurred while inserting restaurant metadata", e)
                throw e
            }
        }

        job.join()
    }

    override suspend fun insert(
        entityList: List<RestaurantMetadata>,
        batchSize: Int
    ): Unit = coroutineScope {
        val jobs = entityList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val documentList = batch.map { entity ->
                    entity.toDocument()
                }
                val options = InsertManyOptions().ordered(false)

                launch {
                    try {
                        val bulkWriteResult = collection.insertMany(documentList, options)
                        logger.debug("Inserted restaurant metadata: ${bulkWriteResult.insertedIds.size}/${batch.size} (batch $idx)")
                    } catch (e: MongoBulkWriteException) {
                        logger.error("Failed to insert restaurant metadata (batch $idx): ${e.message}")
                        for (error in e.writeErrors)
                            logger.error("Error at index ${error.index}: ${error.message}")

                        throw e
                    }
                }
            }

        jobs.joinAll()
    }

    override suspend fun update(
        entity: RestaurantMetadata
    ): Unit = coroutineScope {
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

        val job = launch {
            try {
                val updateResult = collection.updateOne(filter, update)
                logger.debug("Updated restaurant metadata ${entity.id}")
            } catch (e: Exception) {
                logger.error("Failed to update restaurant metadata ${entity.id}: ${e.message}")

                throw e
            }
        }

        job.join()
    }

    override suspend fun update(
        entityList: List<RestaurantMetadata>,
        batchSize: Int
    ): Unit = coroutineScope {
        val jobs = entityList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val updates = batch.map { entity ->
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
                    UpdateOneModel<Document>(filter, update)
                }

                launch {
                    try {
                        val bulkWriteResult = collection.bulkWrite(updates)
                        logger.debug("Updated restaurant metadata: ${bulkWriteResult.modifiedCount}/${batch.size} (batch $idx)")
                    } catch (e: Exception) {
                        logger.error("Failed to update restaurant metadata (batch $idx): ${e.message}")

                        throw e
                    }
                }
            }

        jobs.joinAll()
    }

    override suspend fun delete(
        id: String
    ): Unit = coroutineScope {
        val filter = eq(RestaurantMetadata::id.name, id)

        val job = launch {
            try {
                val deleteResult = collection.deleteOne(filter)
                logger.debug("Deleted restaurant metadata $id")
            } catch (e: Exception) {
                logger.error("Failed to delete restaurant metadata $id: ${e.message}")

                throw e
            }
        }

        job.join()
    }

    override suspend fun delete(
        idList: List<String>,
        batchSize: Int
    ): Unit = coroutineScope {
        val jobs = idList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val deletes = batch.map { id ->
                    val filter = eq(RestaurantMetadata::id.name, id)
                    DeleteOneModel<Document>(filter)
                }

                launch {
                    try {
                        val bulkWriteResult = collection.bulkWrite(deletes)
                        logger.debug(
                            "Deleted restaurant metadata: ${bulkWriteResult.deletedCount}/${batch.size} (batch $idx)"
                        )
                    } catch (e: Exception) {
                        logger.error("Failed to delete restaurant metadata (batch $idx): ${e.message}")

                        throw e
                    }
                }
            }

        jobs.joinAll()
    }

    override suspend fun get(
        id: String
    ): RestaurantMetadata = coroutineScope {
        val filter = eq(RestaurantMetadata::id.name, id)

        val entityDeferred = async {
            try {
                val document = collection.find(filter).first()
                logger.debug("Get restaurant metadata $id")

                RestaurantMetadata.fromDocument(document)
            } catch (e: MongoClientException) {
                logger.error("Failed to get restaurant metadata $id: ${e.message}")

                throw EntityNotFoundException.restaurantMetadataNotFound(id)
            }
        }

        entityDeferred.await()
    }

    override suspend fun get(
        idList: List<String>,
        batchSize: Int
    ): List<RestaurantMetadata> = coroutineScope {
        val entityListDeferred = idList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val filter = `in`(RestaurantMetadata::id.name, batch)

                async {
                    try {
                        val getResult = collection.find(filter).toList()
                        logger.debug("Get restaurant metadata: ${getResult.size}/${batch.size} (batch $idx)")

                        idx to getResult.map { document ->
                            RestaurantMetadata.fromDocument(document)
                        }
                    } catch(e: Exception) {
                        logger.error("Failed to get restaurant metadata (batch $idx): ${e.message}")

                        idx to emptyList()
                    }
                }
            }

            entityListDeferred.awaitAll()
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    override suspend fun getAll(): List<RestaurantMetadata> = coroutineScope {
        val entityListDeferred = async {
            try {
                val getResults = collection.find().toList()
                logger.debug("Get all restaurant metadata")

                getResults.map { document ->
                    RestaurantMetadata.fromDocument(document)
                }
            } catch (e: Exception) {
                logger.error("Failed get all restaurant metadata: ${e.message}")

                throw e
            }
        }

        entityListDeferred.await()
    }

    suspend fun exists(id: String) = coroutineScope {
        val filter = eq(RestaurantMetadata::id.name, id)

        val existDeferred = async {
            try {
                val documentList = collection.find(filter).toList()

                documentList.isNotEmpty()
            } catch (e: Exception) {
                throw e
            }
        }

        existDeferred.await()
    }

    companion object {
        private const val TAG = "RestaurantMetadataRepository"
        private const val DATABASE_NAME = "TasteCompass"
        private const val COLLECTION_NAME = "Restaurant"
    }
}