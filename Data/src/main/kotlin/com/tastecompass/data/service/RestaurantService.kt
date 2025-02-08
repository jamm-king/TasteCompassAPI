package com.tastecompass.data.service

import com.tastecompass.data.common.AnalyzeStep
import com.tastecompass.data.entity.Restaurant
import com.tastecompass.data.entity.RestaurantEmbedding
import com.tastecompass.data.entity.RestaurantMetadata
import com.tastecompass.data.repository.milvus.EmbeddingRepository
import com.tastecompass.data.repository.mongo.MetadataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class RestaurantService(
    private val metadataRepository: MetadataRepository<RestaurantMetadata>,
    private val embeddingRepository: EmbeddingRepository<RestaurantEmbedding>
) : DataStorageService<Restaurant> {

    private val logger = Logger.getLogger(TAG)
    private val updatedRestaurant: MutableSharedFlow<Restaurant> = MutableSharedFlow()
    override fun search(fieldName: String, topK: Int, vector: List<Float>): List<Restaurant> {
        val embeddingList = embeddingRepository.search(fieldName, topK, listOf(vector)).first()
        val idList = embeddingList.map{ it.id }
        val metadataList = metadataRepository.get(idList)
        val embeddingMap = embeddingList.associateBy { it.id }
        return metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }
    }

//    override fun search(fieldName: String, topK: Int, vectorList: List<List<Float>>): List<List<Restaurant>> {
//        val resultList = embeddingRepository.search(fieldName, topK, vectorList)
//        return resultList.map { embeddingList ->
//            val idList = embeddingList.map { it.id }
//            val metadataList = metadataRepository.get(idList)
//            val embeddingMap = embeddingList.associateBy { it.id }
//            metadataList.map { metadata ->
//                val embedding = embeddingMap[metadata.id]
//                Restaurant.createRestaurant(metadata, embedding)
//            }
//        }
//    }

    override fun insert(entity: Restaurant) {
        insert(listOf(entity))
    }

    override fun insert(entityList: List<Restaurant>) {
        val preparedEntities = entityList.filter { it.status == AnalyzeStep.PREPARED }
        if (preparedEntities.isNotEmpty()) {
            handlePreparedEntities(preparedEntities)
        }
    }

    override fun update(entity: Restaurant) {
        update(listOf(entity))
    }

    override fun update(entityList: List<Restaurant>) {
        val analyzedEntities = entityList.filter { it.status == AnalyzeStep.ANALYZED }
        val embeddedEntities = entityList.filter { it.status == AnalyzeStep.EMBEDDED }

        if (analyzedEntities.isNotEmpty()) {
            handleAnalyzedEntities(analyzedEntities)
        }

        if (embeddedEntities.isNotEmpty()) {
            handleEmbeddedEntities(embeddedEntities)
        }
    }

    override fun upsert(entity: Restaurant) {
        upsert(listOf(entity))
    }

    override fun upsert(entityList: List<Restaurant>) {
        val existingIds = metadataRepository.get(entityList.map { it.id }).map { it.id }.toSet()
        val toInsert = entityList.filter { it.id !in existingIds }
        val toUpdate = entityList.filter { it.id in existingIds }

        if (toInsert.isNotEmpty()) {
            insert(toInsert)
        }
        if (toUpdate.isNotEmpty()) {
            update(toUpdate)
        }
    }

    override fun delete(id: String) {
        delete(listOf(id))
    }

    override fun delete(idList: List<String>) {
        metadataRepository.delete(idList)
        embeddingRepository.delete(idList)
    }

    override fun get(id: String): Restaurant? {
        return get(listOf(id)).firstOrNull()
    }

    override fun get(idList: List<String>): List<Restaurant> {
        val metadataList = metadataRepository.get(idList)
        val embeddingList = embeddingRepository.get(idList)
        val embeddingMap = embeddingList.associateBy { it.id }
        return metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }
    }

    override fun getAll(): List<Restaurant> {
        val metadataList = metadataRepository.getAll()
        val embeddingList = embeddingRepository.getAll()
        val embeddingMap = embeddingList.associateBy { it.id }
        return metadataList.map { metadata ->
            val embedding = embeddingMap[metadata.id]
            Restaurant.create(metadata, embedding)
        }
    }

    override fun getAsFlow(): Flow<Restaurant> = updatedRestaurant

    private fun handlePreparedEntities(preparedEntities: List<Restaurant>) {
        try {
            val metadataList = preparedEntities.map { it.metadata }
            metadataRepository.insert(metadataList)
            preparedEntities.forEach { restaurant ->
                CoroutineScope(Dispatchers.IO).launch {
                    updatedRestaurant.emit(restaurant)
                }
            }
        } catch (e: Exception) {
            logger.severe("Failed to handle prepared entities: ${e.message}")
        }
    }

    private fun handleAnalyzedEntities(analyzedEntities: List<Restaurant>) {
        try {
            val metadataList = analyzedEntities.map { it.metadata }
            metadataRepository.update(metadataList)
            analyzedEntities.forEach { restaurant ->
                CoroutineScope(Dispatchers.IO).launch {
                    updatedRestaurant.emit(restaurant)
                } }
        } catch (e: Exception) {
            logger.severe("Failed to handle analyzed entities: ${e.message}")
        }
    }

    private fun handleEmbeddedEntities(embeddedEntities: List<Restaurant>) {
        try {
            val metadataList = embeddedEntities.map { it.metadata }
            val embeddingList = embeddedEntities.map { it.embedding ?: throw Exception("Restaurant id ${it.id} has no embedding") }
            metadataRepository.update(metadataList)
            embeddingRepository.insert(embeddingList)
            embeddedEntities.forEach { restaurant ->
                CoroutineScope(Dispatchers.IO).launch {
                    updatedRestaurant.emit(restaurant)
                } }
        } catch (e: Exception) {
            logger.severe("Failed to handle embedded entities: ${e.message}")
        }
    }

    companion object {
        const val TAG = "RestaurantService"
    }
}

