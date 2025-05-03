package com.tastecompass.data.service

import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Embedding
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.data.exception.InvalidRequestException
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val mongoRepository: MongoRepository<Metadata>,
    private val milvusRepository: MilvusRepository<Embedding>
) : DataService<Restaurant> {

    override suspend fun save(
        entity: Restaurant
    ): Unit = coroutineScope {
        if (entity.status != AnalyzeStep.EMBEDDED) {
            throw InvalidRequestException.invalidSaveState(entity)
        }

        val metadata = entity.metadata
        val embedding = entity.embedding ?: throw Exception("Restaurant id ${entity.id} has no embedding")

        val mongoJob = async { mongoRepository.upsert(metadata) }
        val milvusJob = async { milvusRepository.upsert(embedding) }

        mongoJob.await()
        milvusJob.await()
    }

    override suspend fun search(
        fieldName: String,
        topK: Int,
        vector: List<Float>
    ): List<Restaurant> = coroutineScope {
        val embeddingList = milvusRepository.search(fieldName, topK, vector)
        val metadataMap = mongoRepository.get(embeddingList.map { it.id }).associateBy { it.id }

        embeddingList.mapNotNull { embedding ->
            val metadata = metadataMap[embedding.id]
            metadata?.let { Restaurant.create(it, embedding) }
        }
    }

    override suspend fun getById(
        id: String
    ): Restaurant = coroutineScope {
        try {
            val metadataDeferred = async { mongoRepository.get(id) }
            val embeddingDeferred = async { milvusRepository.get(id) }

            val metadata = metadataDeferred.await()
            val embedding = embeddingDeferred.await()

            Restaurant(
                metadata = metadata,
                embedding = embedding
            )
        } catch(e: EntityNotFoundException) {
            logger.error("Failed to get restaurant: ${e.message}")
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}

