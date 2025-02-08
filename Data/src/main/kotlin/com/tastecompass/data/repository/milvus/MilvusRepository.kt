package com.tastecompass.data.repository.milvus

interface MilvusRepository<T> {
    fun search(fieldName: String, topK: Int, vectorList: List<List<Float>>): List<List<T>>
    fun insert(entityList: List<T>)
    fun upsert(entityList: List<T>)
    fun delete(idList: List<String>)
    fun get(idList: List<String>): List<T>
    fun getAll(): List<T>
}