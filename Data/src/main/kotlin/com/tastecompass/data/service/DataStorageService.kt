package com.tastecompass.data.service

import kotlinx.coroutines.flow.Flow

interface DataStorageService<T> {
    fun search(fieldName: String, topK: Int, vector: List<Float>): List<T>
//    fun search(fieldName: String, topK: Int, vectorList: List<List<Float>>): List<List<T>>
    fun insert(entity: T)
    fun insert(entityList: List<T>)
    fun update(entity: T)
    fun update(entityList: List<T>)
    fun upsert(entity: T)
    fun upsert(entityList: List<T>)
    fun delete(id: String)
    fun delete(idList: List<String>)
    fun get(id: String): T?
    fun get(idList: List<String>): List<T>
    fun getAll(): List<T>
    fun getAsFlow(): Flow<T>
}