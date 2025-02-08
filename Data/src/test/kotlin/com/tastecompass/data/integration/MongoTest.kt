package com.tastecompass.data.integration

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.data.config.MongoConfig
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.logging.Logger

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MongoConfig::class])
class MongoTest {

    @Autowired
    private lateinit var mongoClient: MongoClient

    private val logger = Logger.getLogger(TAG)

    @Test
    fun `should connect to MongoDB Atlas`() {
        val database = mongoClient.getDatabase("admin")
        database.runCommand(Document("ping", 1))
        logger.info("Pinged your deployment. You successfully connected to MongoDB!")
    }

    companion object {
        const val TAG = "com.config.MongoConfigTest"
    }
}
