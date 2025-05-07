package com.tastecompass.embedding.service

import com.tastecompass.domain.common.AnalyzeStep
import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.openai.client.OpenAIClientWrapperImpl
import com.tastecompass.openai.config.OpenAIConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes=[OpenAIConfig::class, OpenAIClientWrapperImpl::class, EmbeddingServiceImpl::class])
class EmbeddingServiceTest {
    @Autowired
    lateinit var embeddingService: EmbeddingService

    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    @Test
    fun `should embed restaurant`(): Unit = runBlocking {
        val restaurant = Restaurant.create(
            id = "test",
            status = AnalyzeStep.ANALYZED,
            mood = listOf("힙한 느낌"),
            taste = listOf("담백하게 맛있다")
        )

        val embeddingResult = embeddingService.embed(restaurant)
        logger.info("mood vector: ${embeddingResult.moodVector}")
        logger.info("taste vector: ${embeddingResult.tasteVector}")
    }
}
