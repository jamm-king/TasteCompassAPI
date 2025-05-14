package com.tastecompass.controller.service

import com.tastecompass.analyzer.dto.FullAnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.controller.identifier.IdGenerator
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Review
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension::class)
class ControllerServiceTest {

    @MockBean
    lateinit var idGenerator: IdGenerator
    @MockBean
    lateinit var analyzerService: AnalyzerService
    @MockBean
    lateinit var embeddingService: EmbeddingService
    @MockBean
    lateinit var dataService: DataService<Restaurant>

    @Autowired
    lateinit var controllerService: ControllerService

    private val dummyText = "분위기가 좋고, 달달한 디저트가 맛있는 카페예요."
    private val analysisResult = FullAnalysisResult(
        name = "TestRestaurant",
        taste = "달콤함",
        mood = "편안함",
        address = "서울시 강남구",
        x = 127.00,
        y = 35.00
    )
    private val embeddingResult = EmbeddingResult(
        moodVector = List(1536) { 0.1f },
        tasteVector = List(1536) { 0.2f }
    )

    @BeforeEach
    fun setup(): Unit = runBlocking {
        whenever(analyzerService.analyze(any())).thenReturn(analysisResult)
        whenever(idGenerator.generate(any())).thenReturn("123")
        whenever(dataService.getById("123")).thenThrow(EntityNotFoundException("Not found"))
        whenever(embeddingService.embed(any())).thenReturn(embeddingResult)
        whenever(dataService.save(any())).thenReturn(Unit)
    }

    @Test
    fun `should process review and save restaurant`() = runBlocking {
        val analyzeLatch = CountDownLatch(1)
        val embedLatch = CountDownLatch(1)
        val saveLatch = CountDownLatch(1)

        whenever(analyzerService.analyze(any())).thenAnswer {
            analyzeLatch.countDown()
            analysisResult
        }
        whenever(embeddingService.embed(any())).thenAnswer {
            embedLatch.countDown()
            embeddingResult
        }
        whenever(dataService.save(any())).thenAnswer {
            saveLatch.countDown()
        }

        val review = Review(
            source = "tistory",
            url = "https://tistory.com/test",
            address = "포항시 남구 이인로 90",
            text = dummyText
        )

        controllerService.receiveReviewData(review)

        assertTrue(analyzeLatch.await(2, TimeUnit.SECONDS))
        assertTrue(embedLatch.await(2, TimeUnit.SECONDS))
        assertTrue(saveLatch.await(2, TimeUnit.SECONDS))
    }

    @TestConfiguration
    class ControllerServiceTestConfiguration {

        @Bean
        fun controllerService(
            idGenerator: IdGenerator,
            analyzerService: AnalyzerService,
            embeddingService: EmbeddingService,
            dataService: DataService<Restaurant>
        ): ControllerService {
            return ControllerService(idGenerator, analyzerService, embeddingService, dataService)
        }
    }
}