package com.tastecompass.controller.service

import com.tastecompass.analyzer.dto.AnalysisResult
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
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ControllerService(
    private val idGenerator: IdGenerator,
    private val analyzerService: AnalyzerService,
    private val embeddingService: EmbeddingService,
    private val dataService: DataService<Restaurant>
): CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + job

    private val reviewFlow = MutableSharedFlow<Review>(extraBufferCapacity = 100)
    private val analyzedFlow = MutableSharedFlow<Restaurant>(extraBufferCapacity = 100)
    private val embeddedFlow = MutableSharedFlow<Restaurant>(extraBufferCapacity = 100)

    @PostConstruct
    fun init() {
        launch { analyzePipeline() }
        launch { embedPipeline() }
        launch { savePipeline() }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down ControllerService...")
        job.cancel()
    }

    fun receiveReviewData(review: Review) {
        launch {
            logger.info("Received review from source: ${review.source}")
            reviewFlow.emit(review)
        }
    }

    private suspend fun analyzePipeline() {
        reviewFlow.collect { review ->
            logger.info("Starting analysis for review from ${review.source}")
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
        }
    }

    private suspend fun embedPipeline() {
        analyzedFlow.collect { restaurant ->
            logger.info("Embedding restaurant with id ${restaurant.id}")
            val embeddingResult = embeddingService.embed(restaurant)
            val embedded = updateRestaurant(restaurant, embeddingResult)
            embeddedFlow.emit(embedded)
        }
    }

    private suspend fun savePipeline() {
        embeddedFlow.collect { restaurant ->
            logger.info("Saving embedded restaurant with id ${restaurant.id} through DataService")
            dataService.save(restaurant)
        }
    }

    private fun createRestaurant(
        id: String,
        review: Review,
        analysisResult: AnalysisResult
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
            taste = listOf(analysisResult.taste)
        )
    }

    private fun updateRestaurant(
        restaurant: Restaurant,
        review: Review,
        analysisResult: AnalysisResult
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

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}
