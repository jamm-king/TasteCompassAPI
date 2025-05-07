package com.tastecompass.application.controller

import com.tastecompass.controller.service.ControllerService
import com.tastecompass.domain.entity.Review
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val controllerService: ControllerService
) {

    @PostMapping
    fun receiveReview(
        @RequestBody review: Review
    ): ResponseEntity<String> {
        val validationError = validateReview(review)
        return if (validationError != null) {
            ResponseEntity.badRequest().body(validationError)
        } else {
            try {
                controllerService.receiveReviewData(review)
                ResponseEntity.ok("Review received")
            } catch (e: Exception) {
                ResponseEntity.internalServerError().body("Error: ${e.message}")
            }
        }
    }

    private fun validateReview(review: Review): String? {
        if (review.source.isBlank()) return "source is required"
        if (review.url.isBlank()) return "url is required"
        if (review.text.isBlank()) return "text is required"
        return null
    }
}