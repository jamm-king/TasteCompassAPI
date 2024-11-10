package com.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
object Constants {
    @Value("\${spring.db.milvus.endpoint}")
    lateinit var ENDPOINT_URI: String
    @Value("\${spring.db.milvus.token}")
    lateinit var BEAR_TOKEN: String
}