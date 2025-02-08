package com.tastecompass.data.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="mongo")
class MongoProperties {
    lateinit var protocol: String
    lateinit var user: String
    lateinit var password: String
    lateinit var host: String
    lateinit var options: String
}