package com.tastecompass.data.repository.mongo

import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.data.config.MongoConfig
import com.tastecompass.domain.entity.RestaurantMetadata
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.system.measureTimeMillis

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MongoConfig::class, RestaurantMetadataRepository::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

class RestaurantMetadataRepositoryPerformanceTest {

    @Autowired
    lateinit var client: MongoClient
    @Autowired
    lateinit var repository: RestaurantMetadataRepository

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val insertedIds = mutableListOf<String>()
    private val entitySize = 1
    private val batchSize = 1

    @BeforeEach
    fun setup() = runBlocking {
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            insertedIds.add(id)
        }

        repository.delete(insertedIds, batchSize)
        insertedIds.clear()
    }

    @AfterEach
    fun cleanup() = runBlocking {
        repository.delete(insertedIds, batchSize)
        insertedIds.clear()
    }

    @Test
    fun `client getOne performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val id = "restaurant-1"
        val metadata = RestaurantMetadata(id = id, status = AnalyzeStep.PREPARED)
        repository.insert(metadata)
        insertedIds.add(id)

        val filter = eq(RestaurantMetadata::id.name, id)

        val elapsed = measureTimeMillis {
            collection.find(filter)
        }
        logger.info("CLIENT:GET_ONE Execution time: $elapsed ms")
    }

    @Test
    fun `client getMany performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val filter = `in`(RestaurantMetadata::id.name, idList)

        val elapsed = measureTimeMillis {
            collection.find(filter)
        }
        logger.info("CLIENT:GET_MANY Execution time: $elapsed ms")
    }

    @Test
    fun `client insertOne performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val id = "restaurant-1"
        val document = RestaurantMetadata(id = id, status = AnalyzeStep.PREPARED).toDocument()

        val elapsed = measureTimeMillis {
            collection.insertOne(document)
        }
        insertedIds.add(id)
        logger.info("CLIENT:INSERT_ONE Execution time: $elapsed ms")
    }

    @Test
    fun `client insertMany performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val idList = mutableListOf<String>()
        val documentList = mutableListOf<Document>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            documentList.add(metadata.toDocument())
        }
        val options = InsertManyOptions().ordered(false)

        val elapsed = measureTimeMillis {
            collection.insertMany(documentList, options)
        }
        insertedIds.addAll(idList)
        logger.info("CLIENT:INSERT_MANY Execution time: $elapsed ms")
    }

    @Test
    fun `client updateOne performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val id = "restaurant-1"
        val metadata = RestaurantMetadata(id = id, status = AnalyzeStep.PREPARED)
        repository.insert(metadata)
        insertedIds.add(id)

        val filter = eq(RestaurantMetadata::id.name, metadata.id)
        val update = combine(
            set(RestaurantMetadata::status.name, metadata.status),
            set(RestaurantMetadata::source.name, metadata.source),
            set(RestaurantMetadata::name.name, metadata.name),
            set(RestaurantMetadata::category.name, metadata.category),
            set(RestaurantMetadata::phone.name, metadata.phone),
            set(RestaurantMetadata::address.name, metadata.address),
            set(RestaurantMetadata::x.name, metadata.x),
            set(RestaurantMetadata::y.name, metadata.y),
            set(RestaurantMetadata::reviews.name, metadata.reviews),
            set(RestaurantMetadata::businessDays.name, metadata.businessDays),
            set(RestaurantMetadata::url.name, metadata.url),
            set(RestaurantMetadata::hasWifi.name, metadata.hasWifi),
            set(RestaurantMetadata::hasParking.name, metadata.hasParking),
            set(RestaurantMetadata::menus.name, metadata.menus),
            set(RestaurantMetadata::minPrice.name, metadata.minPrice),
            set(RestaurantMetadata::maxPrice.name, metadata.maxPrice),
            set(RestaurantMetadata::mood.name, metadata.mood),
            set(RestaurantMetadata::taste.name, metadata.taste)
        )

        val elapsed = measureTimeMillis {
            collection.updateOne(filter, update)
        }
        logger.info("CLIENT:UPDATE_ONE Execution time: $elapsed ms")
    }

    @Test
    fun `client updateMany performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val updates = metadataList.map { metadata ->
            val filter = eq(RestaurantMetadata::id.name, metadata.id)
            val update = combine(
                set(RestaurantMetadata::status.name, metadata.status),
                set(RestaurantMetadata::source.name, metadata.source),
                set(RestaurantMetadata::name.name, metadata.name),
                set(RestaurantMetadata::category.name, metadata.category),
                set(RestaurantMetadata::phone.name, metadata.phone),
                set(RestaurantMetadata::address.name, metadata.address),
                set(RestaurantMetadata::x.name, metadata.x),
                set(RestaurantMetadata::y.name, metadata.y),
                set(RestaurantMetadata::reviews.name, metadata.reviews),
                set(RestaurantMetadata::businessDays.name, metadata.businessDays),
                set(RestaurantMetadata::url.name, metadata.url),
                set(RestaurantMetadata::hasWifi.name, metadata.hasWifi),
                set(RestaurantMetadata::hasParking.name, metadata.hasParking),
                set(RestaurantMetadata::menus.name, metadata.menus),
                set(RestaurantMetadata::minPrice.name, metadata.minPrice),
                set(RestaurantMetadata::maxPrice.name, metadata.maxPrice),
                set(RestaurantMetadata::mood.name, metadata.mood),
                set(RestaurantMetadata::taste.name, metadata.taste)
            )
            UpdateOneModel<Document>(filter, update)
        }

        val elapsed = measureTimeMillis {
            collection.bulkWrite(updates)
        }
        logger.info("CLIENT:UPDATE_MANY Execution time: $elapsed ms")
    }

    @Test
    fun `client deleteOne performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val id = "restaurant-1"
        val metadata = RestaurantMetadata(id = id, status = AnalyzeStep.PREPARED)
        repository.insert(metadata)
        insertedIds.add(id)

        val filter = eq(RestaurantMetadata::id.name, id)

        val elapsed = measureTimeMillis {
            collection.deleteOne(filter)
        }
        logger.info("CLIENT:DELETE_ONE Execution time: $elapsed ms")
    }

    @Test
    fun `client deleteMany performance test`() = runBlocking {
        val database = client.getDatabase(DATABASE_NAME)
        val collection = database.getCollection<Document>(COLLECTION_NAME)

        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val deletes = idList.map { id ->
            val filter = eq(RestaurantMetadata::id.name, id)
            DeleteOneModel<Document>(filter)
        }

        val elapsed = measureTimeMillis {
            collection.bulkWrite(deletes)
        }
        logger.info("CLIENT:DELETE_MANY Execution time: $elapsed ms")
    }

    @Test
    fun `repository get performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val elapsed = measureTimeMillis {
            repository.get(idList, batchSize)
        }
        logger.info("REPO:GET Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository insert performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }

        val elapsed = measureTimeMillis {
            repository.insert(metadataList, batchSize)
        }
        insertedIds.addAll(idList)
        logger.info("REPO:GET Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository update performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val elapsed = measureTimeMillis {
            repository.update(metadataList, batchSize)
        }
        logger.info("REPO:UPDATE Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    @Test
    fun `repository delete performance test`() = runBlocking {
        val idList = mutableListOf<String>()
        val metadataList = mutableListOf<RestaurantMetadata>()
        for(i in 0..entitySize) {
            val id = "Restaurant-$i"
            val metadata = RestaurantMetadata(
                id = id,
                status = AnalyzeStep.PREPARED
            )
            idList.add(id)
            metadataList.add(metadata)
        }
        repository.insert(metadataList, batchSize)
        insertedIds.addAll(idList)

        val elapsed = measureTimeMillis {
            repository.delete(idList, batchSize)
        }
        logger.info("REPO:DELETE Execution time: $elapsed ms (entitySize: $entitySize, batchSize: $batchSize)")
    }

    companion object {
        const val DATABASE_NAME = "TasteCompass"
        const val COLLECTION_NAME = "Restaurant"
    }
}