package com.service.repository

import com.entity.Restaurant
import com.entity.RestaurantProperty
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.vector.request.*
import io.milvus.v2.service.vector.request.data.FloatVec
import org.springframework.stereotype.Repository as SpringRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger

@SpringRepository
class RestaurantRepository(
    private val milvusClient: MilvusClientV2
) : Repository<Restaurant> {
    private val logger: Logger = Logger.getLogger(TAG)
    private val updatedRestaurant: MutableSharedFlow<Restaurant> = MutableSharedFlow()

    override fun insert(entities: List<Restaurant>) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataList = entities.map { it.toJsonObject() }
            val insertReq = InsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(dataList)
                .build()

            milvusClient.insert(insertReq)
            entities.map { updatedRestaurant.emit(it) }
        }
    }

    override fun upsert(entities: List<Restaurant>) {
        val dataList = entities.map { it.toJsonObject() }
        val upsertReq = UpsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()

        milvusClient.upsert(upsertReq)
    }

    override fun delete(ids: List<Long>) {
        val deleteReq = DeleteReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(ids)
            .build()

        milvusClient.delete(deleteReq)
    }

    override fun search(fieldName: String, topK: Int, data: List<List<Float>>): List<List<Restaurant>> {
        val vectorData = data.map { FloatVec(it) }
        val searchReq = SearchReq.builder()
            .collectionName(COLLECTION_NAME)
            .annsField(fieldName)
            .data(vectorData)
            .topK(topK)
            .outputFields(RestaurantProperty.getKeys())
            .build()

        val searchResp = milvusClient.search(searchReq)
        val ret = mutableListOf<MutableList<Restaurant>>()
        searchResp.searchResults.forEach { results ->
            val topKResults = mutableListOf<Restaurant>()
            results.forEach { result ->
                val restaurant = Restaurant.fromMap(result.entity)
                logger.info(restaurant.toReadableString())
                topKResults.add(Restaurant.fromMap(result.entity))
            }
            ret.add(topKResults)
        }

        return ret
    }

    override fun get(ids: List<Long>): List<Restaurant> {
        val getReq = GetReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(ids)
            .build()

        val getResp = milvusClient.get(getReq)
        val ret = mutableListOf<Restaurant>()
        getResp.getResults.forEach { getResult ->
            ret.add(Restaurant.fromMap(getResult.entity))
        }

        return ret
    }

    override fun getAll(): List<Restaurant> {
        val queryReq = QueryReq.builder()
            .collectionName(COLLECTION_NAME)
            .filter("id > -1")
            .build()

        val queryResp = milvusClient.query(queryReq)
        val ret = mutableListOf<Restaurant>()
        queryResp.queryResults.forEach { queryResult ->
            ret.add(Restaurant.fromMap(queryResult.entity))
        }

        return ret
    }

    override fun getAsFlow(): Flow<Restaurant> {
        return updatedRestaurant
    }

    companion object {
        private const val COLLECTION_NAME = "Restaurant"
        private const val TAG = "RestaurantRepository"
    }
}