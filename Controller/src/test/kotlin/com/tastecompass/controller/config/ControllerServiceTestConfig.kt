package com.tastecompass.controller.config

import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.analyzer.service.AnalyzerService
import com.tastecompass.controller.identifier.IdGenerator
import com.tastecompass.controller.service.ControllerService
import com.tastecompass.data.exception.EntityNotFoundException
import com.tastecompass.data.service.DataService
import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.domain.entity.Review
import com.tastecompass.embedding.dto.EmbeddingResult
import com.tastecompass.embedding.service.EmbeddingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestConfiguration
class ControllerServiceTestConfig {

    @MockBean
    lateinit var analyzerService: AnalyzerService
    @MockBean
    lateinit var embeddingService: EmbeddingService
    @MockBean
    lateinit var dataService: DataService<Restaurant>
    @MockBean
    lateinit var idGenerator: IdGenerator

    @Bean
    fun controllerService(
        idGenerator: IdGenerator,
        analyzerService: AnalyzerService,
        embeddingService: EmbeddingService,
        dataService: DataService<Restaurant>
    ): ControllerService {
        return ControllerService(
            idGenerator,
            analyzerService,
            embeddingService,
            dataService
        )
    }
}

