package com.tastecompass.controller.identifier

import com.tastecompass.analyzer.dto.AnalysisResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IdGeneratorTest {
    private val idGenerator = IdGenerator()

    @Test
    fun `generate should return correct hash for valid name and address`() {
        // given
        val name = "백종원의 맛집"
        val address = "서울특별시 중구 을지로12길 34"
        val taste = "담백함"
        val mood = "정겨움"
        val result = AnalysisResult(
            name = name,
            address = address,
            taste = taste,
            mood = mood
        )

        // when
        val id = idGenerator.generate(result)

        // then
        val expectedRoadName = "을지로12길"
        val expectedSource = "$name|$expectedRoadName"
        val expectedHash = expectedSource.toByteArray(Charsets.UTF_8)
            .let { java.security.MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }

        assertEquals(expectedHash, id)
    }

    @Test
    fun `generate should throw exception when road name cannot be extracted`() {
        // given
        val name = "이상한 식당"
        val address = "도로명이 포함되지 않은 주소"
        val taste = "이상한 맛"
        val mood = "이상한 분위기"
        val result = AnalysisResult(
            name = name,
            address = address,
            taste = taste,
            mood = mood
        )

        // when & then
        val exception = assertThrows<RuntimeException> {
            idGenerator.generate(result)
        }

        assertEquals("cannot extract road name from $address", exception.message)
    }
}