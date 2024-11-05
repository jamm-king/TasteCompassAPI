package com.service.repository

import com.entity.Restaurant
import com.config.MilvusConfig
import com.google.gson.JsonArray
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.QueryReq
import io.milvus.v2.service.vector.request.UpsertReq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger

class RestaurantRepository : Repository<Restaurant> {
    override fun insert(entity: Restaurant) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = entity.toJsonObject()
            val collectionName = jsonObject.get("collectionName").asString
            val data = jsonObject.get("data").asJsonArray
            val dataList = data.map { it.asJsonObject }

            val insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(dataList)
                .build()

            milvusClient.insert(insertReq)
            updatedRestaurant.emit(entity)
        }
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override fun update() {
        TODO("Not yet implemented")
    }

    override fun upsert(entity: Restaurant) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = entity.toJsonObject()
            val collectionName = jsonObject.get("collectionName").asString
            val data = jsonObject.get("data").asJsonArray
            val dataList = data.map { it.asJsonObject }

            val upsertReq = UpsertReq.builder()
                .collectionName(collectionName)
                .data(dataList)
                .build()

            milvusClient.upsert(upsertReq)
            updatedRestaurant.emit(entity)
        }
    }

    override fun getAll(): List<Restaurant> {
        val queryReq = QueryReq.builder()
            .collectionName(COLLECTION_NAME)
            .filter("id > -1")
            .build()

        val queryResp = milvusClient.query(queryReq)
        val result = mutableListOf<Restaurant>()
        queryResp.queryResults.forEach { queryResult ->
            result.add(Restaurant.fromMap(queryResult.entity))
        }

        return result
    }

    override fun getAsFlow(): Flow<Restaurant> {
        return updatedRestaurant
    }

    companion object {
        private const val COLLECTION_NAME = "Restaurant"
        private const val TAG = "RestaurantRepository"
        private val logger = Logger.getLogger(TAG)
        private val milvusClient = MilvusConfig().milvusClient()
        private val updatedRestaurant: MutableSharedFlow<Restaurant> = MutableSharedFlow()
    }
}