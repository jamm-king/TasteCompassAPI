package com.tastecompass.service

import java.security.MessageDigest

object CacheKeyGenerator {

    fun analysisKey(query: String): String =
        "analysis::${hashQuery(query)}"

    fun embeddingKey(query: String): String =
        "embedding::${hashQuery(query)}"

    private fun hashQuery(query: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(query.toByteArray())
            .joinToString("") { "%02x".format(it) }
}