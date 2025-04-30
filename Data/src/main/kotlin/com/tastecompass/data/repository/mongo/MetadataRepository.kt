package com.tastecompass.data.repository.mongo

import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoClientException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import kotlinx.coroutines.*
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class MetadataRepository(
    private val mongoClient: MongoClient
): MongoRepository<Metadata> {

    private val database = mongoClient.getDatabase(DATABASE_NAME)
    private val collection = database.getCollection<Document>(COLLECTION_NAME)

    override suspend fun insert(
        entity: Metadata
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
        entityList: List<Metadata>,
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
        entity: Metadata
    ): Unit = coroutineScope {
        val filter = eq(Metadata::id.name, entity.id)
        val update = combine(
            set(Metadata::status.name, entity.status),
            set(Metadata::source.name, entity.source),
            set(Metadata::name.name, entity.name),
            set(Metadata::category.name, entity.category),
            set(Metadata::phone.name, entity.phone),
            set(Metadata::address.name, entity.address),
            set(Metadata::x.name, entity.x),
            set(Metadata::y.name, entity.y),
            set(Metadata::reviews.name, entity.reviews),
            set(Metadata::businessDays.name, entity.businessDays),
            set(Metadata::url.name, entity.url),
            set(Metadata::hasWifi.name, entity.hasWifi),
            set(Metadata::hasParking.name, entity.hasParking),
            set(Metadata::menus.name, entity.menus),
            set(Metadata::minPrice.name, entity.minPrice),
            set(Metadata::maxPrice.name, entity.maxPrice),
            set(Metadata::mood.name, entity.mood),
            set(Metadata::taste.name, entity.taste)
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
        entityList: List<Metadata>,
        batchSize: Int
    ): Unit = coroutineScope {
        val jobs = entityList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val updates = batch.map { entity ->
                    val filter = eq(Metadata::id.name, entity.id)
                    val update = combine(
                        set(Metadata::status.name, entity.status),
                        set(Metadata::source.name, entity.source),
                        set(Metadata::name.name, entity.name),
                        set(Metadata::category.name, entity.category),
                        set(Metadata::phone.name, entity.phone),
                        set(Metadata::address.name, entity.address),
                        set(Metadata::x.name, entity.x),
                        set(Metadata::y.name, entity.y),
                        set(Metadata::reviews.name, entity.reviews),
                        set(Metadata::businessDays.name, entity.businessDays),
                        set(Metadata::url.name, entity.url),
                        set(Metadata::hasWifi.name, entity.hasWifi),
                        set(Metadata::hasParking.name, entity.hasParking),
                        set(Metadata::menus.name, entity.menus),
                        set(Metadata::minPrice.name, entity.minPrice),
                        set(Metadata::maxPrice.name, entity.maxPrice),
                        set(Metadata::mood.name, entity.mood),
                        set(Metadata::taste.name, entity.taste)
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

    override suspend fun upsert(entity: Metadata): Unit = coroutineScope {
        val filter = eq(Metadata::id.name, entity.id)
        val update = combine(
            set(Metadata::status.name, entity.status),
            set(Metadata::source.name, entity.source),
            set(Metadata::name.name, entity.name),
            set(Metadata::category.name, entity.category),
            set(Metadata::phone.name, entity.phone),
            set(Metadata::address.name, entity.address),
            set(Metadata::x.name, entity.x),
            set(Metadata::y.name, entity.y),
            set(Metadata::reviews.name, entity.reviews),
            set(Metadata::businessDays.name, entity.businessDays),
            set(Metadata::url.name, entity.url),
            set(Metadata::hasWifi.name, entity.hasWifi),
            set(Metadata::hasParking.name, entity.hasParking),
            set(Metadata::menus.name, entity.menus),
            set(Metadata::minPrice.name, entity.minPrice),
            set(Metadata::maxPrice.name, entity.maxPrice),
            set(Metadata::mood.name, entity.mood),
            set(Metadata::taste.name, entity.taste)
        )

        val job = launch {
            try {
                val result = collection.updateOne(filter, update, UpdateOptions().upsert(true))
                logger.debug("Upserted restaurant metadata ${entity.id}")
            } catch (e: Exception) {
                logger.error("Failed to upsert restaurant metadata ${entity.id}: ${e.message}")
                throw e
            }
        }

        job.join()
    }


    override suspend fun upsert(entityList: List<Metadata>, batchSize: Int): Unit = coroutineScope {
        val jobs = entityList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val updates = batch.map { entity ->
                    val filter = eq(Metadata::id.name, entity.id)
                    val update = combine(
                        set(Metadata::status.name, entity.status),
                        set(Metadata::source.name, entity.source),
                        set(Metadata::name.name, entity.name),
                        set(Metadata::category.name, entity.category),
                        set(Metadata::phone.name, entity.phone),
                        set(Metadata::address.name, entity.address),
                        set(Metadata::x.name, entity.x),
                        set(Metadata::y.name, entity.y),
                        set(Metadata::reviews.name, entity.reviews),
                        set(Metadata::businessDays.name, entity.businessDays),
                        set(Metadata::url.name, entity.url),
                        set(Metadata::hasWifi.name, entity.hasWifi),
                        set(Metadata::hasParking.name, entity.hasParking),
                        set(Metadata::menus.name, entity.menus),
                        set(Metadata::minPrice.name, entity.minPrice),
                        set(Metadata::maxPrice.name, entity.maxPrice),
                        set(Metadata::mood.name, entity.mood),
                        set(Metadata::taste.name, entity.taste)
                    )

                    UpdateOneModel<Document>(
                        filter,
                        update,
                        UpdateOptions().upsert(true)
                    )
                }

                launch {
                    try {
                        val bulkWriteResult = collection.bulkWrite(updates)
                        logger.debug("Upserted restaurant metadata: ${bulkWriteResult.modifiedCount + bulkWriteResult.upserts.size}/${batch.size} (batch $idx)")
                    } catch (e: Exception) {
                        logger.error("Failed to upsert restaurant metadata (batch $idx): ${e.message}")
                        throw e
                    }
                }
            }

        jobs.joinAll()
    }


    override suspend fun delete(
        id: String
    ): Unit = coroutineScope {
        val filter = eq(Metadata::id.name, id)

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
                    val filter = eq(Metadata::id.name, id)
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
    ): Metadata = coroutineScope {
        val filter = eq(Metadata::id.name, id)

        val entityDeferred = async {
            try {
                val document = collection.find(filter).first()
                logger.debug("Get restaurant metadata $id")

                Metadata.fromDocument(document)
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
    ): List<Metadata> = coroutineScope {
        val entityListDeferred = idList.chunked(batchSize)
            .mapIndexed { idx, batch ->
                val filter = `in`(Metadata::id.name, batch)

                async {
                    try {
                        val getResult = collection.find(filter).toList()
                        logger.debug("Get restaurant metadata: ${getResult.size}/${batch.size} (batch $idx)")

                        idx to getResult.map { document ->
                            Metadata.fromDocument(document)
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

    override suspend fun getAll(): List<Metadata> = coroutineScope {
        val entityListDeferred = async {
            try {
                val getResults = collection.find().toList()
                logger.debug("Get all restaurant metadata")

                getResults.map { document ->
                    Metadata.fromDocument(document)
                }
            } catch (e: Exception) {
                logger.error("Failed get all restaurant metadata: ${e.message}")

                throw e
            }
        }

        entityListDeferred.await()
    }

    suspend fun exists(id: String) = coroutineScope {
        val filter = eq(Metadata::id.name, id)

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

    override suspend fun getByName(
        name: String
    ): Metadata = coroutineScope {
        val filter = eq(Metadata::name.name, name)

        val entityDeferred = async {
            try {
                val document = collection.find(filter).first()
                logger.debug("Get restaurant metadata $name")

                Metadata.fromDocument(document)
            } catch (e: MongoClientException) {
                logger.error("Failed to get restaurant metadata $name: ${e.message}")

                throw EntityNotFoundException.restaurantMetadataNotFound(name)
            }
        }

        entityDeferred.await()
    }

    companion object {
        private const val DATABASE_NAME = "TasteCompass"
        private const val COLLECTION_NAME = "Restaurant"
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}