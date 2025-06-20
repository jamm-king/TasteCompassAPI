package com.tastecompass.data.config

import com.tastecompass.domain.common.Constants
import com.tastecompass.data.common.MilvusProperties
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.common.DataType
import io.milvus.v2.common.IndexParam
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.HasCollectionReq
import io.milvus.v2.service.index.request.CreateIndexReq
import io.milvus.v2.service.index.request.ListIndexesReq
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
    @Bean
    open fun milvusClient(): MilvusClientV2 {
        var client: MilvusClientV2? = null
        var attempt = 0

        while (true) {
            try {
                client = MilvusClientV2(
                    ConnectConfig.builder()
                        .uri(milvusProperties.endpoint)
                        .token(milvusProperties.token)
                        .connectTimeoutMs(10000)
                        .build()
                )

                ensureCollectionExists(client, milvusProperties.collectionName)
                ensureIndexExists(client, milvusProperties.collectionName, milvusProperties.vectorFiledNames)
                loadCollection(client, milvusProperties.collectionName)

                return client

            } catch (ex: Exception) {
                client?.close()
                attempt++
                if (attempt >= maxRetries) throw ex

                logger.error("Milvus Client connection failed. ${attempt}’th trying…")
                Thread.sleep(retryDelayMs)
            }
        }
    }

    private val collectionLocks = ConcurrentHashMap<String, Any>()

    private fun ensureCollectionExists(
        client: MilvusClientV2,
        collectionName: String
    ) {
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

    private fun ensureIndexExists(
        client: MilvusClientV2,
        collectionName: String,
        vectorFieldNames: List<String>
    ) {
        val lock = collectionLocks.computeIfAbsent(collectionName) { Any() }
        synchronized(lock) {
            val existingIndexNames = client.listIndexes(
                ListIndexesReq.builder()
                    .collectionName(milvusProperties.collectionName)
                    .build()
            )

            vectorFieldNames.forEach { fieldName ->
                val hasIndex = existingIndexNames.any { indexName ->
                    indexName.contains(fieldName, ignoreCase = true)
                }

                if(!hasIndex) {
                    logger.info("Index on '$fieldName' not found. Creating...")
                    val indexParam = IndexParam.builder()
                        .fieldName(fieldName)
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .metricType(IndexParam.MetricType.COSINE)
                        .build()
                    client.createIndex(
                        CreateIndexReq.builder()
                            .collectionName(milvusProperties.collectionName)
                            .indexParams(listOf(indexParam))
                            .build()
                    )
                    logger.info("Index on '$fieldName' created.")
                } else {
                    logger.info("Index for field '$fieldName' already exists.")
                }
            }
        }
    }

    private fun loadCollection(
        client: MilvusClientV2,
        collectionName: String
    ) {
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
        private const val maxRetries = 3
        private const val retryDelayMs = 2000L
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)

        fun getRestaurantFieldSchemas() = listOf(

            CreateCollectionReq.FieldSchema.builder()
                .name("id").description("Primary key")
                .dataType(DataType.VarChar).maxLength(200)
                .isPrimaryKey(true).autoID(false).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("category").dataType(DataType.VarChar).maxLength(200).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("address").dataType(DataType.VarChar).maxLength(200).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("x").dataType(DataType.Double).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("y").dataType(DataType.Double).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("businessDays").dataType(DataType.VarChar).maxLength(200).build(),

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
                .dimension(Constants.EMBEDDING_SIZE).build(),

            CreateCollectionReq.FieldSchema.builder()
                .name("categoryVector").dataType(DataType.FloatVector)
                .dimension(Constants.EMBEDDING_SIZE).build()
        )
    }
}
