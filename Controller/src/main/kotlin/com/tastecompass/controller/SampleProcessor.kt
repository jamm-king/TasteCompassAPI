package com.tastecompass.controller

import com.tastecompass.data.common.Constants
import com.tastecompass.data.entity.Restaurant
import com.tastecompass.analyzer.DefaultAnalyzer
import com.tastecompass.data.service.RestaurantService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class SampleProcessor(
    private val restaurantService: RestaurantService
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
                val getData = fetchData(restaurantService)
                val analyzeData = analyze(getData)
                store(analyzeData)

                // Check DB data for debugging
                restaurantService.getAll().also {
                    logger.info("Total DB count : ${it.size}")
                    it.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
                delay(5000)

                // Delete test
                scope?.launch {
                    val ids = listOf(1L.toString(), 3L.toString())
                    restaurantService.delete(ids)
                    logger.info("deleted records of id 1, 3")
                }
                delay(5000)

                // Check DB data for debugging
                restaurantService.getAll().also {
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
                        val data = Restaurant.create(
                            id = idx.toString(),
                            name = "Sample Restaurant $idx",
                            mood = System.currentTimeMillis().toString(),
                            moodVector = List(1536) { idx.toFloat() },
                            minPrice = 10000 * idx,
                            maxPrice = 20000 * idx
                        )
                        restaurantService.insert(listOf(data))
                    }
                }
                delay(12000)

                // Check DB data for debugging
                restaurantService.getAll().also {
                    logger.info("Total DB count : ${it.size}")
                    it.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
                delay(5000)

                // Search test
                scope?.launch {
                    val moodVector = List(Constants.EMBEDDING_SIZE) { 3.0f }
                    val searchResult = restaurantService.search("mood_vector", 7, moodVector)
                    logger.info("result search for mood_vector [3.0, 3.0, 3.0, ...]")
                    searchResult.forEach { restaurant ->
                        logger.info(restaurant.toReadableString())
                    }
                }
            } ?: logger.warning("scope is null")
        }
    }

    override fun stop() {
        logger.info("stop")
        scope?.cancel()
    }

    private fun CoroutineScope.fetchData(service: RestaurantService): ReceiveChannel<Restaurant> = produce {
        service.getAsFlow().collect {
            logger.info("collect data - ${it.name}")
            send(it)
        }
    }

    private fun CoroutineScope.analyze(channel: ReceiveChannel<Restaurant>) = produce {
        channel.consumeEach { sample ->
            logger.info("analyzing [${sample.name}]")
            scope?.run {
                val result = DefaultAnalyzer(this).analyze(sample.name ?: "")
                val updated = sample.update(mood = result)
                logger.info("process pipeline done! [${updated.name}] $result")
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
                restaurantService.upsert(listOf(result))
            }
        }
    }

    companion object {
        private const val TAG = "SampleProcessor"
        private val logger = Logger.getLogger(TAG)
    }
}