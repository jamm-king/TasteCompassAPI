package com.tastecompass.data.service

import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Embedding
import com.tastecompass.domain.entity.Metadata
import com.tastecompass.data.exception.InvalidRequestException
import com.tastecompass.data.repository.milvus.MilvusRepository
import com.tastecompass.data.repository.mongo.MongoRepository
import com.tastecompass.data.saga.SagaContext
import com.tastecompass.data.saga.SagaCoordinator
import com.tastecompass.data.saga.SagaStep
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RestaurantService(
    private val mongoRepo: MongoRepository<Metadata>,
    private val milvusRepo: MilvusRepository<Embedding>,
    private val saga: SagaCoordinator
) : DataService<Restaurant> {

    override suspend fun save(
        entity: Restaurant
    ): Unit = coroutineScope {
        if (entity.status != AnalyzeStep.EMBEDDED) {
            throw InvalidRequestException.invalidSaveState(entity)
        }

        val sagaId = UUID.randomUUID().toString()
        val context = SagaContext(sagaId).apply {
            put("restaurantId", entity.id)
            put("entity", entity)
        }

        val original: Restaurant? = try {
            getById(entity.id)
        } catch (e: EntityNotFoundException) {
            null
        }
        val isNew = (original == null)
        context.put("isNew", isNew)
        original?.let { context.put("original", it) }

        val mongoStep = SagaStep<Unit>(
            name = "mongo",
            action = { ctx ->
                val meta = ctx.get<Restaurant>("entity")!!.metadata
                if (ctx.get<Boolean>("isNew") == true) {
                    mongoRepo.insert(meta)
                } else {
                    mongoRepo.update(meta)
                }
            },
            compensation = { ctx, _ ->
                val id = ctx.get<String>("restaurantId")!!
                if (ctx.get<Boolean>("isNew") == true) {
                    mongoRepo.delete(id)
                } else {
                    val origMeta = ctx.get<Restaurant>("original")!!.metadata
                    mongoRepo.update(origMeta)
                }
            }
        )

        val milvusStep = SagaStep<Unit>(
            name = "milvus",
            action = { ctx ->
                val emb = ctx.get<Restaurant>("entity")!!.embedding!!
                if (ctx.get<Boolean>("isNew") == true) {
                    milvusRepo.insert(emb)
                } else {
                    milvusRepo.upsert(emb)
                }
            },
            compensation = { ctx, _ ->
                val id = ctx.get<String>("restaurantId")!!
                if (ctx.get<Boolean>("isNew") == true) {
                    milvusRepo.delete(id)
                } else {
                    val origEmb = ctx.get<Restaurant>("original")!!.embedding!!
                    milvusRepo.upsert(origEmb)
                }
            }
        )

        saga.execute(context, listOf(mongoStep, milvusStep))
    }

    override suspend fun search(
        fieldName: String,
        topK: Int,
        vector: List<Float>
    ): List<Restaurant> = coroutineScope {
        try {
            val embeddingList = milvusRepo.search(fieldName, topK, vector)
            val metadataMap = mongoRepo.get(embeddingList.map { it.id }).associateBy { it.id }

            embeddingList.mapNotNull { embedding ->
                val metadata = metadataMap[embedding.id]
                metadata?.let { Restaurant.create(it, embedding) }
            }
        } catch(e: Exception) {
            logger.error("Failed to search restaurant: ${e.message}")
            throw e
        }
    }

    override suspend fun hybridSearch(
        fieldToVector: Map<String, List<Float>>,
        topK: Int
    ): List<Restaurant> = coroutineScope {
        try {
            val hybridResults = milvusRepo.hybridSearch(fieldToVector, topK)
            val ids = hybridResults.map { it.id }
            val metadataMap = mongoRepo.get(ids).associateBy { it.id }

            hybridResults.mapNotNull { embedding ->
                val meta = metadataMap[embedding.id]
                meta?.let { Restaurant.create(it, embedding) }
            }
        } catch (e: Exception) {
            logger.error("Failed to hybridSearch restaurants: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getById(
        id: String
    ): Restaurant = coroutineScope {
        try {
            val metadataDeferred = async { mongoRepo.get(id) }
            val embeddingDeferred = async { milvusRepo.get(id) }

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
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

