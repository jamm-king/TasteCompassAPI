package com.tastecompass.data.repository.milvus

import com.tastecompass.domain.entity.Embedding
import com.tastecompass.data.exception.DataAccessException
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.exception.InvalidRequestException
import io.milvus.common.clientenum.ConsistencyLevelEnum
import io.milvus.grpc.SearchResults
import io.milvus.param.R
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.ConsistencyLevel
import io.milvus.v2.exception.MilvusClientException
import io.milvus.v2.service.vector.request.*
import io.milvus.v2.service.vector.request.data.FloatVec
import io.milvus.v2.service.vector.request.ranker.RRFRanker
import io.milvus.v2.service.vector.response.SearchResp
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class EmbeddingRepository(
    private val milvusClient: MilvusClientV2
): MilvusRepository<Embedding> {
    override suspend fun search(
        fieldName: String,
        topK: Int,
        vector: List<Float>,
    ): List<Embedding> = coroutineScope {
        val vectorData = listOf(FloatVec(vector))
        val searchReq = SearchReq.builder()
            .collectionName(COLLECTION_NAME).annsField(fieldName)
            .data(vectorData).topK(topK).outputFields(OUTPUT_FIELDS).build()

        val entityListDeferred = async {
            try {
                val searchResp = milvusClient.search(searchReq)
                val searchResult = searchResp.searchResults.first()
                val entityList = searchResult.map { Embedding.fromMap(it.entity) }
                logger.debug("Searched restaurant embedding (field: $fieldName, topK: $topK)")

                entityList
            } catch (e: MilvusClientException) {
                logger.error("Failed to search restaurant embedding (filed: $fieldName, topK: $topK): ${e.message}")

                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        entityListDeferred.await()
    }

    override suspend fun hybridSearch(
        fieldToVector: Map<String, List<Float>>,
        topK: Int
    ): List<Embedding> = coroutineScope {
        val searchRequests = fieldToVector.map { (fieldName, vector) ->
            AnnSearchReq.builder()
                .vectorFieldName(fieldName)
                .vectors(listOf(FloatVec(vector)))
                .params("{\"nprobe\": 10}")
                .topK(topK)
                .build()
        }

        val hybridReq = HybridSearchReq.builder()
            .collectionName(COLLECTION_NAME)
            .searchRequests(searchRequests)
            .ranker(RRFRanker(20))
            .topK(topK)
            .consistencyLevel(ConsistencyLevel.BOUNDED)
            .outFields(OUTPUT_FIELDS)
            .build()

        val entityListDeferred = async {
            try {
                val searchResp = milvusClient.hybridSearch(hybridReq)
                val searchResult = searchResp.searchResults.first()
                val entityList = searchResult.map { Embedding.fromMap(it.entity) }

                entityList
            } catch (e: MilvusClientException) {
                logger.error("Failed to perform hybridSearch on Milvus: ${e.message}", e)
                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        entityListDeferred.await()
    }

    override suspend fun insert(
        entity: Embedding
    ): Unit = coroutineScope {
        val existingIdList = existingIds(listOf(entity.id))
        if(entity.id in existingIdList)
            throw InvalidRequestException.duplicateKey(entity)

        val dataList = listOf(entity.toJsonObject())
        val insertReq = InsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()

        val insertRespDeferred = async {
            try {
                val insertResp = milvusClient.insert(insertReq)
                if (insertResp.insertCnt == 0L) throw Exception()
                logger.debug("Inserted restaurant embedding ${entity.id}")

                insertResp
            } catch (e: MilvusClientException) {
                logger.error("Failed to insert restaurant embedding ${entity.id}: ${e.message}")

                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        insertRespDeferred.await()
    }

    override suspend fun insert(
        entityList: List<Embedding>,
        batchSize: Int
    ): Unit = coroutineScope {
        val insertRespListDeferred = entityList.chunked(batchSize).mapIndexed { idx, batch ->
            val existingIdList = existingIds(batch.map{ it.id })
            val filteredList = batch.filter { it.id !in existingIdList }
            existingIdList.map { id ->
                launch {
                    throw InvalidRequestException.duplicateKey(entityList.first { it.id == id })
                }
            }

            val dataList = filteredList.map { it.toJsonObject() }
            val insertReq = InsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(dataList)
                .build()

            async {
                try {
                    val insertResp = milvusClient.insert(insertReq)
                    logger.debug("Inserted restaurant embedding: ${insertResp.insertCnt}/${batch.size} (batch $idx)")

                    insertResp
                } catch(e: MilvusClientException) {
                    logger.error("Failed to insert restaurant embedding (batch $idx): ${e.message}")

                    throw DataAccessException.milvusAccessUnavailable()
                }
            }
        }

        insertRespListDeferred.awaitAll()
    }

    override suspend fun upsert(
        entity: Embedding
    ): Unit = coroutineScope {
        val dataList = listOf(entity.toJsonObject())
        val upsertReq = UpsertReq.builder()
            .collectionName(COLLECTION_NAME)
            .data(dataList)
            .build()
        launch {
            try {
                milvusClient.upsert(upsertReq)
                logger.debug("Upserted restaurant embedding ${entity.id}")
            } catch (e: MilvusClientException) {
                logger.error("Failed to upsert restaurant embedding ${entity.id}: ${e.message}")
                throw DataAccessException.milvusAccessUnavailable()
            }
        }
    }

    override suspend fun upsert(
        entityList: List<Embedding>,
        batchSize: Int
    ): Unit = coroutineScope {
        val upsertRespListDeferred = entityList.chunked(batchSize).mapIndexed { idx, batch ->
            val dataList = batch.map { it.toJsonObject() }
            val upsertReq = UpsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(dataList)
                .build()

            async {
                try {
                    val upsertResp = milvusClient.upsert(upsertReq)
                    logger.debug("Upserted restaurant embedding: ${upsertResp.upsertCnt}/${batch.size} (batch $idx)")

                    upsertResp
                } catch(e: MilvusClientException) {
                    logger.error("Failed to upsert restaurant embedding (batch $idx): ${e.message}")

                    throw DataAccessException.milvusAccessUnavailable()
                }
            }
        }

        upsertRespListDeferred.awaitAll()
    }

    override suspend fun delete(
        id: String
    ): Unit = coroutineScope {
        val idList = listOf(id)
        val deleteReq = DeleteReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(idList)
            .build()

        val deleteRespDeferred = async {
            try {
                val deleteResp = milvusClient.delete(deleteReq)
                logger.error("Deleted restaurant embedding $id")

                deleteResp
            } catch (e: MilvusClientException) {
                logger.debug("Failed to delete restaurant embedding $id: ${e.message}")

                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        deleteRespDeferred.await()
    }

    override suspend fun delete(
        idList: List<String>,
        batchSize: Int
    ): Unit = coroutineScope {
        val deleteRespDeferred = idList.chunked(batchSize).mapIndexed { idx, batch ->
            val deleteReq = DeleteReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(batch)
                .build()

            async {
                try {
                    val deleteResp = milvusClient.delete(deleteReq)
                    logger.debug("Deleted restaurant embedding: ${deleteResp.deleteCnt}/${batch.size} (batch $idx)")

                    deleteResp
                } catch (e: MilvusClientException) {
                    logger.error("Failed to delete restaurant embedding (batch $idx): ${e.message}")

                    throw DataAccessException.milvusAccessUnavailable()
                }
            }
        }

        deleteRespDeferred.awaitAll()
    }

    override suspend fun get(
        id: String
    ): Embedding = coroutineScope {
        val idList = listOf(id)
        val getReq = GetReq.builder()
            .collectionName(COLLECTION_NAME)
            .ids(idList)
            .build()

        val entityDeferred = async {
            try {
                val getResp = milvusClient.get(getReq)
                val getResults = getResp.getResults

                if (getResults.isEmpty()) {
                    logger.error("restaurant embedding with id $id is not found")
                    throw EntityNotFoundException.restaurantEmbeddingNotFound(id)
                }

                val entity = Embedding.fromMap(getResults.first().entity)
                logger.debug("Get restaurant embedding $id")

                entity
            } catch (e: MilvusClientException) {
                logger.error("Failed to get restaurant embedding $id: ${e.message}")

                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        entityDeferred.await()
    }

    override suspend fun get(
        idList: List<String>,
        batchSize: Int
    ): List<Embedding> = coroutineScope {
        val entityListDeferred = idList.chunked(batchSize).mapIndexed { idx, batch ->
            val getReq = GetReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(batch)
                .build()

            async {
                try {
                    val getResp = milvusClient.get(getReq)
                    val getResults = getResp.getResults
                    val entityList = getResults.map { Embedding.fromMap(it.entity) }
                    logger.debug("Get restaurant embedding: ${getResults.size}/${batch.size} (batch $idx)")

                    idx to entityList
                } catch(e: MilvusClientException) {
                    logger.error("Failed to get restaurant embedding (batch $idx): ${e.message}")

                    throw DataAccessException.milvusAccessUnavailable()
                }
            }
        }

        entityListDeferred.awaitAll()
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    override suspend fun getAll(): List<Embedding> = coroutineScope {
        val queryReq = QueryReq.builder()
            .collectionName(COLLECTION_NAME).filter("id != \"\"").build()

        val entityListDeferred = async {
            try {
                val queryResp = milvusClient.query(queryReq)
                val queryResults = queryResp.queryResults
                val entityList = queryResults.map { Embedding.fromMap(it.entity) }
                logger.debug("Get all restaurant embedding")

                entityList
            } catch (e: MilvusClientException) {
                logger.error("Failed to get all restaurant embedding")

                throw DataAccessException.milvusAccessUnavailable()
            }
        }

        entityListDeferred.await()
    }

    private suspend fun existingIds(
        idList: List<String>,
        batchSize: Int = 100
    ): List<String> = coroutineScope {
        val existingIdsDeferred = idList.chunked(batchSize).mapIndexed { idx, batch ->
            val getReq = GetReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(batch)
                .build()

            async {
                try {
                    val getResp = milvusClient.get(getReq)
                    val getResults = getResp.getResults
                    val existingIdList = getResults.map { it.entity["id"] as String }

                    idx to existingIdList
                } catch(e: MilvusClientException) {
                    throw DataAccessException.milvusAccessUnavailable()
                }
            }
        }

        existingIdsDeferred.awaitAll()
            .sortedBy { it.first }
            .flatMap { it.second }
    }


    companion object {
        private const val TAG = "RestaurantEmbeddingRepository"
        private const val COLLECTION_NAME = "Restaurant"
        private val OUTPUT_FIELDS = listOf(
            "id",
            "category",
            "address",
            "x",
            "y",
            "businessDays",
            "hasWifi",
            "hasParking",
            "minPrice",
            "maxPrice",
            "moodVector",
            "tasteVector"
        )
        private const val BATCH_SIZE = 100
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}