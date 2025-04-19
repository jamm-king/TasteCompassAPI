package com.tastecompass.data.config

import com.tastecompass.data.common.Constants
import com.tastecompass.data.common.MilvusProperties
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.common.DataType
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.HasCollectionReq
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import java.util.concurrent.ConcurrentHashMap

@Configuration
@PropertySource("classpath:milvus.properties")
@EnableConfigurationProperties(MilvusProperties::class)
open class MilvusConfig (
    private val milvusProperties: MilvusProperties
) {
    private val maxRetries = 3
    private val retryDelayMs = 2000L
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    open fun milvusClient(): MilvusClientV2 {
        return retry(maxRetries, retryDelayMs) {
            val client = MilvusClientV2(
                ConnectConfig.builder()
                    .uri(milvusProperties.endpoint)
                    .token(milvusProperties.token)
                    .connectTimeoutMs(10000)
                    .build()
            )

            COLLECTION_NAMES.forEach { collectionName ->
                ensureCollectionExists(client, collectionName)
                loadCollection(client, collectionName)
            }

            client
        }
    }

    private fun <T> retry(maxAttempts: Int, delayMs: Long, block: () -> T): T {
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (ex: Exception) {
                attempt++
                if (attempt >= maxAttempts) throw ex
                logger.error("Milvus Client connection failed. ${attempt}'th trying...")
                runBlocking { delay(delayMs) }
            }
        }
    }

    private val collectionLocks = ConcurrentHashMap<String, Any>()

    private fun ensureCollectionExists(client: MilvusClientV2, collectionName: String) {
        val lock = collectionLocks.computeIfAbsent(collectionName) { Any() }
        synchronized(lock) {
            val hasCollection = client.hasCollection(
                HasCollectionReq.builder()
                    .collectionName(collectionName)
                    .build()
            )
            if (!hasCollection) {
                logger.info("Collection '$collectionName' does not exist. Creating...")

                val createCollectionReq = CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .collectionSchema(
                        CreateCollectionReq.CollectionSchema.builder()
                            .fieldSchemaList(getRestaurantFieldSchemas())
                            .build()
                    )
                    .build()

                client.createCollection(createCollectionReq)
                logger.info("Collection '$collectionName' created successfully.")
            } else {
                logger.info("Collection '$collectionName' already exists.")
            }
        }
    }

    private fun loadCollection(client: MilvusClientV2, collectionName: String) {
        val lock = collectionLocks.computeIfAbsent(collectionName) { Any() }
        synchronized(lock) {
            try {
                logger.info("Loading collection '$collectionName' into memory...")

                client.loadCollection(
                    io.milvus.v2.service.collection.request.LoadCollectionReq.builder()
                        .collectionName(collectionName)
                        .build()
                )

                logger.info("Collection '$collectionName' successfully loaded into memory.")
            } catch (ex: Exception) {
                logger.error("Failed to load collection '$collectionName': ${ex.message}")
                throw ex
            }
        }
    }

    companion object {
        val COLLECTION_NAMES = listOf(
            "Restaurant"
        )

        fun getRestaurantFieldSchemas() = listOf(

            CreateCollectionReq.FieldSchema.builder()
                .name("id").description("Primary key")
                .dataType(DataType.VarChar).maxLength(100)
                .isPrimaryKey(true).autoID(false).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("category").dataType(DataType.VarChar).maxLength(50).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("address").dataType(DataType.VarChar).maxLength(50).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("x").dataType(DataType.Double).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("y").dataType(DataType.Double).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("businessDays").dataType(DataType.VarChar).maxLength(100).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("hasWifi").dataType(DataType.Bool).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("hasParking").dataType(DataType.Bool).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("minPrice").dataType(DataType.Int32).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("maxPrice").dataType(DataType.Int32).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("moodVector").dataType(DataType.FloatVector)
                .dimension(Constants.EMBEDDING_SIZE).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("tasteVector").dataType(DataType.FloatVector)
                .dimension(Constants.EMBEDDING_SIZE).build()
        )
    }
}
