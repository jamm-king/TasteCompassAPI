package com.tastecompass.embedding

import java.util.Collections.emptyList
import java.util.logging.Logger

class VectorEmbeddingDelegate {

    fun getEmbedding(text: String) : List<Double> {
        logger.info("Embedding... $text")
        return emptyList()
    }

    companion object {
        private const val TAG = "VectorEmbeddingDelegate"
        private val logger = Logger.getLogger(TAG)
    }
}