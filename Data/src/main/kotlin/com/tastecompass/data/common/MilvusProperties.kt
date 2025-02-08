package com.tastecompass.data.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="milvus")
class MilvusProperties {
    lateinit var endpoint: String
    lateinit var token: String
}