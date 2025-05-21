package com.tastecompass.data.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import com.tastecompass.data.common.MongoProperties
import com.tastecompass.data.exception.DataAccessException
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
@PropertySource("classpath:mongo.properties")
@EnableConfigurationProperties(MongoProperties::class)
open class MongoConfig(
    private val mongoProperties: MongoProperties
) {
    @Bean
    open fun connectionString(): ConnectionString {
        val uri = "${mongoProperties.protocol}://${mongoProperties.user}:${mongoProperties.password}@${mongoProperties.host}/?${mongoProperties.options}"
        return ConnectionString(uri)
    }

    @Bean
    open fun serverApi(): ServerApi {
        return ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
    }

    @Bean
    open fun mongoClientSettings(
        connectionString: ConnectionString,
        serverApi: ServerApi
    ): MongoClientSettings {
        return MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .serverApi(serverApi)
            .build()
    }

    @Bean
    open fun mongoClient(mongoClientSettings: MongoClientSettings): MongoClient {
        return retry(maxRetries, retryDelayMs) {
            val client = MongoClient.create(mongoClientSettings)
            ensureCollectionExists(client, mongoProperties.collectionName)

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
                logger.error("Mongo Client connection failed. ${attempt}'th trying...")
                runBlocking { delay(delayMs) }
            }
        }
    }

    private val collectionLocks = ConcurrentHashMap<String, Any>()

    private fun checkDatabaseExist(mongoClient: MongoClient) {
        if(!mongoClient.listDatabaseNames().toList().contains(mongoProperties.databaseName)) {
            logger.error("Database '${mongoProperties.databaseName}' does not exist. Create through MongoDB Atlas.")

            throw DataAccessException.databaseNotExist(mongoProperties.databaseName)
        }
    }

    private fun ensureCollectionExists(mongoClient: MongoClient, collectionName: String) {
        val lock = collectionLocks.computeIfAbsent(collectionName) { Any() }
        synchronized(lock) {
            try {
                checkDatabaseExist(mongoClient)
            } catch(e: DataAccessException) {
                logger.error("Failed to load database '${mongoProperties.databaseName}': ${e.message}")

                return
            }
            val database: MongoDatabase = mongoClient.getDatabase(mongoProperties.databaseName)

            if (!database.listCollectionNames().toList().contains(collectionName)) {
                logger.info("Collection '$collectionName' does not exist. Creating...")
                database.createCollection(collectionName)
                logger.info("Collection '$collectionName' created successfully.")
            } else {
                logger.info("Collection '$collectionName' already exists.")
            }
        }
    }

    companion object {
        private const val maxRetries = 3
        private const val retryDelayMs = 2000L
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}