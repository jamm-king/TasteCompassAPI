package com.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.coroutines.suspendCoroutine

class DefaultAnalyzer(private val scope: CoroutineScope): Analyzer {
    override suspend fun analyze(text: String): String {
        return suspendCoroutine { continuation ->
            logger.info("Analyze and extract info")
            scope.launch {
                logger.info("Embedding start")
                VectorEmbeddingDelegate().getEmbedding(text)
                delay(2000)
                continuation.resumeWith(Result.success("Success!"))
            }
        }
    }

    companion object {
        private const val TAG = "DefaultAnalyzer"
        private val logger = Logger.getLogger(TAG)
    }
}