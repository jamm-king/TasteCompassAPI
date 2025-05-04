package com.tastecompass.controller.service

import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.controller.identifier.IdGenerator
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Review
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension

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

    @BeforeEach
    fun setup(): Unit = runBlocking {
        val dummyResult = AnalysisResult(
            name = "TestRestaurant",
            taste = "달콤함",
            mood = "편안함",
            address = "서울시 강남구"
        )
        val embeddingResult = EmbeddingResult(
            moodVector = List(1536) { 0.1 },
            tasteVector = List(1536) { 0.2 }
        )

        whenever(analyzerService.analyze(any())).thenReturn(dummyResult)
        whenever(idGenerator.generate(any())).thenReturn("123")
        whenever(dataService.getById("123")).thenThrow(EntityNotFoundException("Not found"))
        whenever(embeddingService.embed(any())).thenReturn(embeddingResult)
        whenever(dataService.save(any())).thenReturn(Unit)
    }

    @Test
    fun `should process review and save restaurant`() = runBlocking {
        val review = Review(
            source = "tistory",
            url = "https://tistory.com/test",
            text = dummyText
        )

        controllerService.receiveReviewData(review)

        delay(1000)

        verify(analyzerService).analyze(any())
        verify(embeddingService).embed(any())
        verify(dataService).save(any())
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