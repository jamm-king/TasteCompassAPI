package com.tastecompass.data.service

interface DataService<T> {
    suspend fun save(entity: T)
    suspend fun search(fieldName: String, topK: Int, vector: List<Float>): List<T>
    suspend fun getByName(name: String): T
}