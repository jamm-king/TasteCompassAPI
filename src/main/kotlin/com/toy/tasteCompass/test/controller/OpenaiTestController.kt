package com.toy.tasteCompass.test.controller

import com.toy.tasteCompass.test.dto.OpenaiEmbeddingRequest
import com.toy.tasteCompass.test.dto.OpenaiEmbeddingResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/test")
class OpenaiTestController {
    @Value("\${spring.ai.openai.api-key}")
    lateinit var apiKey: String
    @Value("\${spring.ai.openai.embedding.options.model}")
    lateinit var embeddingModel: String
    @Value("\${spring.ai.openai.base-url}")
    lateinit var openaiBaseUrl: String
    @Value("\${spring.ai.openai.embedding.embeddings-path}")
    lateinit var embeddingUrl: String

    private val restTemplate = RestTemplate()

    @PostMapping("/openai/embedding")
    fun requestEmbeddingApi(@RequestBody request: OpenaiEmbeddingRequest): ResponseEntity<OpenaiEmbeddingResponse> {
        val apiUrl = openaiBaseUrl + embeddingUrl
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $apiKey")
        val body = mapOf(
                "input" to request.input,
                "model" to embeddingModel
        )

        val entity = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(apiUrl, entity, OpenaiEmbeddingResponse::class.java)
        return ResponseEntity.ok(response.body)
    }
}
