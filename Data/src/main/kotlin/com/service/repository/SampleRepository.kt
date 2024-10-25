package com.service.repository

import com.entity.SampleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Todo: Need Dependency Inject
class SampleRepository : Repository{
    override fun insert() {

    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    override fun update() {
        TODO("Not yet implemented")
    }

    override fun getAll() : List<SampleData> {
        return (0 until 10).map {
            SampleData("Data Number ${it}")
        }
    }

    override fun getAsFlow(): Flow<SampleData> {
        return flow {
            (0 until 10).map {
                delay(1000)
                emit(SampleData("Data Number ${it}"))
            }
        }
    }
}