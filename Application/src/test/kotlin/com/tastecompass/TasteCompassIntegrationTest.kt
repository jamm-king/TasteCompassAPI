package com.tastecompass

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes=[TasteCompassApplication::class]
)
@ActiveProfiles("test")
class TasteCompassIntegrationTest {
    @Test
    fun `application runs with test resources`() { /*…*/ }
}