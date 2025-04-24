package com.tastecompass.data.service

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.RestaurantEmbedding
import com.tastecompass.domain.entity.RestaurantMetadata
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val mongoRepository: MongoRepository<RestaurantMetadata>,
    private val milvusRepository: MilvusRepository<RestaurantEmbedding>
) : DataStorageService<Restaurant> {

    private val updatedRestaurant: MutableSharedFlow<Restaurant> = MutableSharedFlow(extraBufferCapacity = 10)
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun search(
        fieldName: String,
        topK: Int,
        vector: List<Float>
    ): List<Restaurant> = coroutineScope {
        val embeddingListDeferred = async { milvusRepository.search(fieldName, topK, vector) }
        val embeddingList = embeddingListDeferred.await()
        val idList = embeddingList.map { it.id }
        val metadataListDeferred = async { mongoRepository.get(idList) }
        val metadataList = metadataListDeferred.await()
        val embeddingMap = embeddingList.associateBy { it.id }

        metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }
    }

    override suspend fun insert(
        entity: Restaurant
    ) = coroutineScope {
        if(entity.status != AnalyzeStep.PREPARED)
            throw InvalidRequestException.invalidInsertState(entity)

        try {
            handlePreparedEntity(entity)
        } catch (e: InvalidRequestException) {
            logger.error("Failed to handle prepared entity: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error occurred while handling prepared entity", e)
            throw e
        }
    }

    override suspend fun insert(entityList: List<Restaurant>): Unit = coroutineScope {
        val preparedEntities = entityList.filter { it.status == AnalyzeStep.PREPARED }

        if(preparedEntities.isEmpty())
            throw InvalidRequestException.invalidInsertState()

        handlePreparedEntities(preparedEntities)
    }

    override suspend fun update(
        entity: Restaurant
    ): Unit = coroutineScope {
        when (entity.status) {
            AnalyzeStep.PREPARED -> throw InvalidRequestException.invalidUpdateState(entity)
            AnalyzeStep.ANALYZED -> handleAnalyzedEntity(entity)
            AnalyzeStep.EMBEDDED -> handleEmbeddedEntity(entity)
        }
    }

    override suspend fun update(
        entityList: List<Restaurant>
    ): Unit = coroutineScope {
        val analyzedEntities = entityList.filter { it.status == AnalyzeStep.ANALYZED }
        val embeddedEntities = entityList.filter { it.status == AnalyzeStep.EMBEDDED }

        if(analyzedEntities.isEmpty() && embeddedEntities.isEmpty())
            throw InvalidRequestException.invalidUpdateState()

        if (analyzedEntities.isNotEmpty())
            handleAnalyzedEntities(analyzedEntities)
        if (embeddedEntities.isNotEmpty())
            handleEmbeddedEntities(embeddedEntities)
    }

    override suspend fun upsert(
        entity: Restaurant
    ): Unit = coroutineScope {
        var isExisting = true
        try {
            mongoRepository.get(entity.id)
        } catch(e: EntityNotFoundException) {
            isExisting = false
        }

        when(isExisting) {
            true -> update(entity)
            false -> insert(entity)
        }
    }

    override suspend fun upsert(
        entityList: List<Restaurant>
    ): Unit = coroutineScope {
        val existingIdsDeferred = async {
            mongoRepository.get(entityList.map { it.id })
                .map { it.id }
                .toSet()
        }
        val existingIds = existingIdsDeferred.await()
        val toInsert = entityList.filter { it.id !in existingIds }
        val toUpdate = entityList.filter { it.id in existingIds }

        if (toInsert.isNotEmpty())
            insert(toInsert)
        if (toUpdate.isNotEmpty())
            update(toUpdate)
    }

    override suspend fun delete(
        id: String
    ): Unit = coroutineScope {
        try {
            val milvusJob = launch { milvusRepository.delete(id) }
            val mongoJob = launch { mongoRepository.delete(id) }

            milvusJob.join()
            mongoJob.join()
        } catch(e: Exception) {
            throw e
        }
    }

    override suspend fun delete(
        idList: List<String>
    ): Unit = coroutineScope {
        try {
            val milvusJob = launch { milvusRepository.delete(idList) }
            val mongoJob = launch { mongoRepository.delete(idList) }

            milvusJob.join()
            mongoJob.join()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun get(id: String): Restaurant {
        return get(listOf(id)).firstOrNull() ?: throw EntityNotFoundException.restaurantNotFound(id)
    }

    override suspend fun get(
        idList: List<String>
    ): List<Restaurant> = coroutineScope {
        val metadataDeferred = async { mongoRepository.get(idList) }
        val embeddingDeferred = async { milvusRepository.get(idList) }

        val metadataList = metadataDeferred.await()
        val embeddingList = embeddingDeferred.await()

        val embeddingMap = embeddingList.associateBy { it.id }
        val entityList = metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }

        entityList
    }

    override suspend fun getAll(): List<Restaurant> = coroutineScope {
        val metadataDeferred = async { mongoRepository.getAll() }
        val embeddingDeferred = async { milvusRepository.getAll() }

        val metadataList = metadataDeferred.await()
        val embeddingList = embeddingDeferred.await()
        val embeddingMap = embeddingList.associateBy { it.id }

        metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }
    }

    override fun getAsFlow(): Flow<Restaurant> = updatedRestaurant

    private suspend fun handlePreparedEntity(preparedEntity: Restaurant) = coroutineScope {
        try {
            val metadata = preparedEntity.metadata
            val job = launch { mongoRepository.insert(metadata) }

            job.join()
            updatedRestaurant.emit(preparedEntity)
        } catch (e: InvalidRequestException) {
            logger.error("Failed to handle prepared entity: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error occurred while handling prepared entity", e)
            throw e
        }
    }


    private  suspend fun handlePreparedEntities(preparedEntities: List<Restaurant>) = coroutineScope {
        try {
            val metadataList = preparedEntities.map { it.metadata }
            val job = launch { mongoRepository.insert(metadataList) }

            job.join()
            preparedEntities.asFlow()
                .onEach { restaurant -> updatedRestaurant.emit(restaurant) }
        } catch (e: Exception) {
            logger.error("Failed to handle prepared entities: ${e.message}")
        }
    }

    private suspend fun handleAnalyzedEntity(analyzedEntity: Restaurant) = coroutineScope {
        try {
            val metadata = analyzedEntity.metadata
            val job = launch { mongoRepository.update(metadata) }

            job.join()
            updatedRestaurant.emit(analyzedEntity)
        } catch (e: Exception) {
            logger.error("Failed to handle analyzed entities: ${e.message}")
        }
    }

    private suspend fun handleAnalyzedEntities(analyzedEntities: List<Restaurant>) = coroutineScope {
        try {
            val metadataList = analyzedEntities.map { it.metadata }
            val job = launch { mongoRepository.update(metadataList) }

            job.join()
            analyzedEntities.asFlow()
                .onEach { restaurant -> updatedRestaurant.emit(restaurant) }
        } catch (e: Exception) {
            logger.error("Failed to handle analyzed entities: ${e.message}")
        }
    }

    private suspend fun handleEmbeddedEntity(embeddedEntity: Restaurant) = coroutineScope {
        try {
            val metadata = embeddedEntity.metadata
            val embedding = embeddedEntity.embedding ?: throw Exception("Restaurant id ${embeddedEntity.id} has no embedding")
            val mongoJob = launch { mongoRepository.update(metadata) }
            val milvusJob = launch { milvusRepository.upsert(embedding) }

            mongoJob.join()
            milvusJob.join()
        } catch (e: Exception) {
            logger.error("Failed to handle embedded entities: ${e.message}")
        }
    }

    private suspend fun handleEmbeddedEntities(embeddedEntities: List<Restaurant>) = coroutineScope {
        try {
            val metadataList = embeddedEntities.map { it.metadata }
            val embeddingList = embeddedEntities.map { it.embedding ?: throw Exception("Restaurant id ${it.id} has no embedding") }
            val mongoJob = launch { mongoRepository.update(metadataList) }
            val milvusJob = launch { milvusRepository.upsert(embeddingList) }

            mongoJob.join()
            milvusJob.join()
        } catch (e: Exception) {
            logger.error("Failed to handle embedded entities: ${e.message}")
        }
    }

    companion object {
        const val TAG = "RestaurantService"
    }
}

