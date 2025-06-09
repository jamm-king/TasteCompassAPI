package com.tastecompass.embedding.service

import com.tastecompass.embedding.dto.EmbeddingRequest
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
        val embeddingReq = EmbeddingRequest(
            mood = "시끄럽지 않고 조용합니다",
            taste = "담백하게 맛있어요",
            category = "한식"
        )

        val embeddingResult = embeddingService.embed(embeddingReq)
        logger.info("mood vector: ${embeddingResult.moodVector}")
        logger.info("taste vector: ${embeddingResult.tasteVector}")
    }
}
