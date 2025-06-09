package com.tastecompass.application.controller

import com.tastecompass.domain.entity.Restaurant
import com.tastecompass.service.SearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchService
) {
    @GetMapping
    suspend fun search(
        @RequestParam q: String?
    ): ResponseEntity<Any> {
        if (q.isNullOrBlank()) {
            return ResponseEntity
                .badRequest()
                .body("query is required.")
        }

        return try {
            val results: List<Restaurant> = searchService.search(q.trim(), 9)
            ResponseEntity.ok(results)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message ?: "Wrong request.")
        } catch (e: Exception) {
            ResponseEntity
                .internalServerError()
                .body("Failed to search: ${e.message}")
        }
    }
}
