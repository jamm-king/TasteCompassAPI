package com.tastecompass.analyzer.service

import com.google.gson.Gson
import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.domain.entity.Review
import com.tastecompass.openai.client.OpenAIClientWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AnalyzerServiceMockTest {

    private lateinit var client: OpenAIClientWrapper
    private lateinit var analyzerService: AnalyzerService
    private val gson = Gson()

    @BeforeEach
    fun setup() {
        client = mock()
        analyzerService = AnalyzerServiceImpl(client)
    }

    @Test
    fun `should analyze review and return valid result`() {
        // given
        val review = Review(
            text = "This restaurant had amazing sushi!",
            source = "naver",
            url = "naver.com/test"
        )
        val expectedResult = AnalysisResult(
            name = "Sushi Place",
            address = "Tokyo, Japan",
            taste = "Delicious",
            mood = "Cozy",
        )

        val responseJson = gson.toJson(expectedResult)

        whenever(client.chat(any())).thenReturn(responseJson)

        // when
        val result = analyzerService.analyze(review)

        // then
        assertEquals(expectedResult, result)
        verify(client).chat(any())
    }

    @Test
    fun `should throw exception when OpenAI client fails`() {
        // given
        val review = Review(
            text = "Great ramen.",
            source = "blog",
            url = "blog.com/test"
        )
        whenever(client.chat(any())).thenThrow(RuntimeException())

        // when + then
        val exception = assertThrows(RuntimeException::class.java) {
            analyzerService.analyze(review)
        }

        verify(client).chat(any())
    }

    @Test
    fun `should throw exception when analysis result is invalid`() {
        // given
        val review = Review(
            text = "Nice cafe",
            source = "tistory",
            url = "tistory.com/test"
        )

        val invalidResultJson = gson.toJson(
            AnalysisResult(
                name = "",
                address = "Seoul",
                taste = "Sweet",
                mood = "",
            )
        )

        whenever(client.chat(any())).thenReturn(invalidResultJson)

        // when + then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            analyzerService.analyze(review)
        }

        // assertTrue(exception.message.contains("Missing required fields"))
        verify(client).chat(any())
    }
}