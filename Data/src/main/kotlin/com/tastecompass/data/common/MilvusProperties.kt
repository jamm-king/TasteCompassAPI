package com.tastecompass.data.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="milvus")
class MilvusProperties {
    lateinit var endpoint: String
    lateinit var token: String
    lateinit var collectionName: String
    var vectorFiledNames: List<String> = mutableListOf()
}