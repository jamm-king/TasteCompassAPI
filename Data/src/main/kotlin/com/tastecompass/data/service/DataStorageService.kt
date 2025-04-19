package com.tastecompass.data.service

import kotlinx.coroutines.flow.Flow

interface DataStorageService<T> {
    suspend fun search(fieldName: String, topK: Int, vector: List<Float>): List<T>
    suspend fun insert(entity: T)
    suspend fun insert(entityList: List<T>)
    suspend fun update(entity: T)
    suspend fun update(entityList: List<T>)
    suspend fun upsert(entity: T)
    suspend fun upsert(entityList: List<T>)
    suspend fun delete(id: String)
    suspend fun delete(idList: List<String>)
    suspend fun get(id: String): T?
    suspend fun get(idList: List<String>): List<T>
    suspend fun getAll(): List<T>
    fun getAsFlow(): Flow<T>
}