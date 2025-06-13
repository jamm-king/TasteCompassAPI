package com.tastecompass.data.service

interface DataService<T> {
    suspend fun save(entity: T)
    suspend fun search(fieldName: String, topK: Int, vector: List<Float>): List<T>
    suspend fun hybridSearch(
        fieldToVector: Map<String, List<Float>>,
        fieldToWeight: Map<String, Float>,
        topK: Int
    ): List<T>
    suspend fun getById(id: String): T
}
