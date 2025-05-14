package com.tastecompass.controller.service

import com.tastecompass.analyzer.dto.FullAnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.controller.identifier.IdGenerator
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.RestaurantMenu
import com.tastecompass.domain.entity.RestaurantProperty
import com.tastecompass.domain.entity.Review
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@Service
class ControllerService(
    private val idGenerator: IdGenerator,
    private val analyzerService: AnalyzerService,
    private val embeddingService: EmbeddingService,
    private val dataService: DataService<Restaurant>
) : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.IO + job

    private val reviewFlow = MutableSharedFlow<Review>(replay = 1, extraBufferCapacity = 100)
    private val analyzedFlow = MutableSharedFlow<Restaurant>(replay = 1, extraBufferCapacity = 100)
    private val embeddedFlow = MutableSharedFlow<Restaurant>(replay = 1, extraBufferCapacity = 100)

    private var pipelineJob: Job? = null
    private var timerJob: Job? = null
    private val lastReceiveTime = AtomicReference(Instant.now())

    companion object {
        private val logger = LoggerFactory.getLogger(ControllerService::class.java)
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down ControllerService...")
        job.cancel()
    }

    fun receiveReviewData(review: Review) {
        launch {
            reviewFlow.emit(review)
        }

        lastReceiveTime.set(Instant.now())
        if (pipelineJob == null || pipelineJob?.isActive == false) {
            launchPipeline()
        }
    }

    private fun launchPipeline() {
        logger.info("Launching pipelines...")
        pipelineJob = launch {
            launch { analyzePipeline() }
            launch { embeddingPipeline() }
            launch { savePipeline() }
        }

        timerJob?.cancel()
        timerJob = launch {
            while (isActive) {
                delay(Duration.ofMinutes(1).toMillis())
                val elapsed = Duration.between(lastReceiveTime.get(), Instant.now())
                if (elapsed.toMinutes() >= 5) {
                    logger.info("No review received in the last 5 minutes. Cancelling pipeline...")
                    pipelineJob?.cancel()
                    pipelineJob = null
                    break
                }
            }
        }
    }

    private suspend fun analyzePipeline() {
        reviewFlow.collect { review ->
            try {
                logger.info("Analyzing review from ${review.source}")
                val analysisResult = analyzerService.analyze(review)
                val id = idGenerator.generate(analysisResult)

                val analyzed = try {
                    val restaurant = dataService.getById(id)
                    logger.info("Found existing restaurant with id $id, updating...")
                    updateRestaurant(restaurant, review, analysisResult)
                } catch (e: EntityNotFoundException) {
                    logger.info("No existing restaurant found for id $id, creating new one")
                    createRestaurant(id, review, analysisResult)
                }
                analyzedFlow.emit(analyzed)
            } catch (e: Exception) {
                logger.error("Error in analyze pipeline: ${e.message}", e)
            }
        }
    }

    private suspend fun embeddingPipeline() {
        analyzedFlow.collect { restaurant ->
            try {
                logger.info("Embedding restaurant with id ${restaurant.id}")
                val embeddingResult = embeddingService.embed(restaurant)
                val embedded = updateRestaurant(restaurant, embeddingResult)
                embeddedFlow.emit(embedded)
            } catch (e: Exception) {
                logger.error("Error in embedding pipeline: ${e.message}", e)
            }
        }
    }

    private suspend fun savePipeline() {
        embeddedFlow.collect { restaurant ->
            try {
                logger.info("Saving embedded restaurant with id ${restaurant.id}")
                dataService.save(restaurant)
            } catch (e: Exception) {
                logger.error("Error in save pipeline: ${e.message}", e)
            }
        }
    }

    private fun createRestaurant(
        id: String,
        review: Review,
        analysisResult: FullAnalysisResult
    ): Restaurant {
        return Restaurant.create(
            id = id,
            status = AnalyzeStep.ANALYZED,
            source = review.source,
            name = analysisResult.name,
            category = analysisResult.category ?: RestaurantProperty.CATEGORY.defaultValue as String,
            phone = analysisResult.phone ?: RestaurantProperty.PHONE.defaultValue as String,
            address = analysisResult.address,
            businessDays = analysisResult.businessDays ?: RestaurantProperty.BUSINESS_DAYS.defaultValue as String,
            hasWifi = analysisResult.hasWifi ?: RestaurantProperty.HAS_WIFI.defaultValue as Boolean,
            hasParking = analysisResult.hasParking ?: RestaurantProperty.HAS_PARKING.defaultValue as Boolean,
            menus = analysisResult.menus ?: RestaurantProperty.MENUS.defaultValue as List<RestaurantMenu>,
            minPrice = analysisResult.minPrice ?: RestaurantProperty.MIN_PRICE.defaultValue as Int,
            maxPrice = analysisResult.maxPrice ?: RestaurantProperty.MAX_PRICE.defaultValue as Int,
            mood = listOf(analysisResult.mood),
            taste = listOf(analysisResult.taste),
            x = analysisResult.x,
            y = analysisResult.y
        )
    }

    private fun updateRestaurant(
        restaurant: Restaurant,
        review: Review,
        analysisResult: FullAnalysisResult
    ): Restaurant {
        return restaurant
            .addReview(review.text)
            .addTaste(analysisResult.taste)
            .addMood(analysisResult.mood)
    }

    private fun updateRestaurant(
        restaurant: Restaurant,
        embeddingResult: EmbeddingResult
    ): Restaurant {
        return restaurant
            .updateMoodVector(embeddingResult.moodVector)
            .updateTasteVector(embeddingResult.tasteVector)
    }
}


