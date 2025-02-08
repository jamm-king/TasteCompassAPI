package com.tastecompass.data.integration

import com.tastecompass.data.config.MilvusConfig
import io.milvus.v2.client.MilvusClientV2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[MilvusConfig::class])
class MilvusTest {

    @Autowired
    private lateinit var milvusClient : MilvusClientV2

    @Test
    fun `should connect to zilliz Milvus cluster`() {
        val listDatabasesResp = milvusClient.listDatabases()
        val dbNames = listDatabasesResp.databaseNames
        assertNotNull(dbNames)
    }
}