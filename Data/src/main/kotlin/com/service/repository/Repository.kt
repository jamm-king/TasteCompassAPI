package com.service.repository

import com.entity.SampleData
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun insert()
    fun delete()
    fun update()
    fun getAll(): List<SampleData>
    fun getAsFlow() : Flow<SampleData>
}