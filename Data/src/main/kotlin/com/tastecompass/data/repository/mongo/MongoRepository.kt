package com.tastecompass.data.repository.mongo

interface MongoRepository<T> {
    suspend fun insert(entity: T)
    suspend fun insert(entityList: List<T>, batchSize: Int = 100)
    suspend fun update(entity: T)
    suspend fun update(entityList: List<T>, batchSize: Int = 100)
    suspend fun upsert(entity: T)
    suspend fun upsert(entityList: List<T>, batchSize: Int = 100)
    suspend fun delete(id: String)
    suspend fun delete(idList: List<String>, batchSize: Int = 100)
    suspend fun get(id: String): T
    suspend fun get(idList: List<String>, batchSize: Int = 100): List<T>
    suspend fun getAll(): List<T>
}