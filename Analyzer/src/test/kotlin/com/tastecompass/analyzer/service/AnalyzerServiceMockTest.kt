package com.tastecompass.analyzer.service

import com.google.gson.Gson
import com.tastecompass.analyzer.dto.FullAnalysisResult
import com.tastecompass.analyzer.dto.OpenAIAnalysisResult
import com.tastecompass.domain.entity.Review
import com.tastecompass.kakao.client.KakaoMapClient
import com.tastecompass.kakao.dto.GeocodeResult
import com.tastecompass.openai.client.OpenAIClientWrapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AnalyzerServiceMockTest {

    private lateinit var openaiClient: OpenAIClientWrapper
    private lateinit var kakaomapClient: KakaoMapClient
    private lateinit var analyzerService: AnalyzerService
    private val gson = Gson()

    @BeforeEach
    fun setup() {
        openaiClient = mock()
        kakaomapClient = mock()
        analyzerService = AnalyzerServiceImpl(openaiClient, kakaomapClient)
    }

    @Test
    fun `should analyze review and return valid result`(): Unit = runBlocking {
        // given
        val review = Review(
            text = "This restaurant had amazing sushi!",
            source = "naver",
            url = "naver.com/test"
        )
        val expectedAnalysisResult = OpenAIAnalysisResult(
            name = "Sushi Place",
            address = "Tokyo, Japan",
            taste = "Delicious",
            mood = "Cozy",
        )
        val responseJson = gson.toJson(expectedAnalysisResult)
        val expectedGeocode = GeocodeResult(
            normalizedAddress = "Tokyo, Japan",
            x = 127.00,
            y = 35.00
        )
        val expectedResult = FullAnalysisResult(
            name = "Sushi Place",
            address = "Tokyo, Japan",
            taste = "Delicious",
            mood = "Cozy",
            x = 127.00,
            y = 35.00
        )

        whenever(openaiClient.chat(any())).thenReturn(responseJson)
        whenever(kakaomapClient.geocode(any())).thenReturn(expectedGeocode)

        // when
        val result = analyzerService.analyze(review)

        // then
        assertEquals(expectedResult, result)
        verify(openaiClient).chat(any())
        verify(kakaomapClient).geocode(any())
    }

    @Test
    fun `should throw exception when OpenAI client fails`(): Unit = runBlocking {
        // given
        val review = Review(
            text = "Great ramen.",
            source = "blog",
            url = "blog.com/test"
        )
        whenever(openaiClient.chat(any())).thenThrow(RuntimeException())

        // when + then
        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                analyzerService.analyze(review)
            }
        }

        verify(openaiClient).chat(any())
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
            OpenAIAnalysisResult(
                name = "",
                address = "Seoul",
                taste = "Sweet",
                mood = "",
            )
        )

        whenever(openaiClient.chat(any())).thenReturn(invalidResultJson)

        // when + then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                analyzerService.analyze(review)
            }
        }

        // assertTrue(exception.message.contains("Missing required fields"))
        verify(openaiClient).chat(any())
    }
}