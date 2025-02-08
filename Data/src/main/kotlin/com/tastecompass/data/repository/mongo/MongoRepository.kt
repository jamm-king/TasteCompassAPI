package com.tastecompass.data.repository.mongo

interface MongoRepository<T> {
    fun insert(entityList: List<T>)
    fun update(entityList: List<T>)
    fun delete(idList: List<String>)
    fun get(idList: List<String>): List<T>
    fun getAll(): List<T>
}