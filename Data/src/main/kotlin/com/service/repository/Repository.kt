package com.service.repository

import kotlinx.coroutines.flow.Flow

interface Repository<T> {
    fun insert(entities: List<T>)
    fun upsert(entities: List<T>)
    fun delete(ids: List<Long>)
    fun search(fieldName: String, topK: Int, data: List<List<Float>>): List<List<T>>
    fun get(ids: List<Long>): List<T>
    fun getAll(): List<T>
    fun getAsFlow(): Flow<T>
}