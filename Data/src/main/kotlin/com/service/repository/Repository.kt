package com.service.repository

import kotlinx.coroutines.flow.Flow

interface Repository<Entity> {
    fun insert(entity: Entity)
    fun delete()
    fun update()
    fun upsert(entity: Entity)
    fun getAll(): List<Entity>
    fun getAsFlow() : Flow<Entity>
}