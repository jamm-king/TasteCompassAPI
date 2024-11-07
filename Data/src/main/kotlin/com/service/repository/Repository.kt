package com.service.repository

import com.entity.Restaurant
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun insert()
    fun delete()
    fun update()
    fun getAll(): List<Restaurant>
    fun getAsFlow() : Flow<Restaurant>
}