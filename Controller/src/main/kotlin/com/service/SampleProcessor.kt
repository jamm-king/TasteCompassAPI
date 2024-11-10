package com.service

import com.common.Constants
import com.entity.Restaurant
import com.service.repository.RestaurantRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class SampleProcessor(
    private val repository: RestaurantRepository
) : Processor {
    private var scope: CoroutineScope? = null

    override fun start(parentScope: CoroutineScope) {
        logger.info("start")

        scope = scope?.let {
            if (!it.isActive) {
                CoroutineScope(Dispatchers.IO)
            }
            it
        } ?: CoroutineScope(Dispatchers.IO)

        parentScope.launch {
            scope?.launch {
                logger.info("Start pipeline")
                val getData = fetchData(repository)
                val analyzeData = analyze(getData)
                store(analyzeData)

                // Check DB data for debugging
                repository.getAll().also {
                    logger.info("Total DB count : ${it.size}")
                    it.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
                delay(5000)

                // Delete test
                scope?.launch {
                    val ids = listOf(1L, 3L)
                    repository.delete(ids)
                    logger.info("deleted records of id 1, 3")
                }
                delay(5000)

                // Check DB data for debugging
                repository.getAll().also {
                    logger.info("Total DB count : ${it.size}")
                    it.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
                delay(5000)

                // Insert test
                scope?.launch {
                    (1 until 6).forEach { idx ->
                        delay(1000)
                        val data = Restaurant(
                            id = idx.toLong(),
                            name = "Sample Restaurant $idx",
                            mood = System.currentTimeMillis().toString(),
                            moodVector = List(1536) { idx.toFloat() },
                            minPrice = 10000.0f * idx,
                            maxPrice = 20000.0f * idx
                        )
                        repository.insert(listOf(data))
                    }
                }
                delay(12000)

                // Check DB data for debugging
                repository.getAll().also {
                    logger.info("Total DB count : ${it.size}")
                    it.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
                delay(5000)

                // Search test
                scope?.launch {
                    val moodVector = List(Constants.EMBEDDING_SIZE) { 3.0f }
                    val searchResults = repository.search("mood_vector", 7, listOf(moodVector))
                    logger.info("result search for mood_vector [3.0, 3.0, 3.0, ...]")
                    for (i in searchResults.indices) {
                        val searchResult = searchResults[i]
                        searchResult.forEach { restaurant ->
                            logger.info(restaurant.toReadableString())
                        }
                    }
                }
            } ?: logger.warning("scope is null")
        }
    }

    override fun stop() {
        logger.info("stop")
        scope?.cancel()
    }

    private fun CoroutineScope.fetchData(repository: RestaurantRepository): ReceiveChannel<Restaurant> = produce {
        repository.getAsFlow().collect {
            logger.info("collect data - ${it.name}")
            send(it)
        }
    }

    private fun CoroutineScope.analyze(channel: ReceiveChannel<Restaurant>) = produce {
        channel.consumeEach { sample ->
            logger.info("analyzing [${sample.name}]")
            scope?.run {
                val result = DefaultAnalyzer(this).analyze(sample.name ?: "")
                sample.mood = result
                logger.info("process pipeline done! [${sample.name}] $result")
                sample.mood?.run {
                    send(sample)
                }
            }
        }
    }

    private fun CoroutineScope.store(channel: ReceiveChannel<Restaurant>) = launch {
        channel.consumeEach { result ->
            logger.info("storing [${result.name}]")
            scope?.run {
                repository.upsert(listOf(result))
            }
        }
    }

    companion object {
        private const val TAG = "SampleProcessor"
        private val logger = Logger.getLogger(TAG)
    }
}