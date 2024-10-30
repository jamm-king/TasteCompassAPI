package com.service.repository

import com.common.Constants.BEAR_TOKEN
import com.common.Constants.BODY_GET_ALL
import com.common.Constants.DB_INSERT_URL
import com.common.Constants.DB_QUERY_URL
import com.entity.Restaurant
import com.service.http.OkHttpHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Todo: Need Dependency Inject
class RestaurantRepository : Repository {

    private val updatedRestaurant: MutableSharedFlow<Restaurant> = MutableSharedFlow()

    override fun insert() {
        CoroutineScope(Dispatchers.IO).launch {
            // Make synthetic restaurant data
            val time = System.currentTimeMillis()
            val data = Restaurant(
                id = time,
                name = "가게명 - $time"
            )
            OkHttpHelper.postRequest(DB_INSERT_URL, data.toJsonObject().toString(), BEAR_TOKEN)
            updatedRestaurant.emit(data)
        }
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override fun update() {
        TODO("Not yet implemented")
    }

    override fun getAll() : List<Restaurant> {
        val response = OkHttpHelper.postRequest(DB_QUERY_URL, BODY_GET_ALL, BEAR_TOKEN)
        val result = response?.let {
            val jsonArray = it.getJSONArray("data")
            val data = mutableListOf<Restaurant>()
            for (i in 0 until jsonArray.length()) {
                data.add(Restaurant.fromJsonObject(jsonArray.getJSONObject(i)))
            }
            data
        } ?: listOf()
        return result
    }

    override fun getAsFlow(): Flow<Restaurant> {
        return updatedRestaurant
    }
}