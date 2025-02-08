package com.tastecompass.data.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.MongoClient
import com.tastecompass.data.common.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

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
        return MongoClient.create(mongoClientSettings)
    }
}