package com.tastecompass.data.saga

class SagaContext(val sagaId: String) {

    private val data = mutableMapOf<String, Any?>()

    fun <T> put(key: String, value: T) { data[key] = value }
    fun <T> get(key: String): T? = data[key] as? T
}