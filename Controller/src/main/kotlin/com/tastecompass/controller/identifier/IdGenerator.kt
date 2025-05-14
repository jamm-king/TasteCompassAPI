package com.tastecompass.controller.identifier

import com.tastecompass.analyzer.dto.FullAnalysisResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class IdGenerator {

    fun generate(result: FullAnalysisResult): String {
        val name = result.name.trim()
        val address = result.address

        return try {
            val normalizedAddress = normalize(address)
            val roadName = extractRoadName(normalizedAddress)
            val source = "$name|$roadName"

            logger.debug("Generating ID with name='{}', address='{}'", name, address)
            logger.debug("Normalized address: '{}'", normalizedAddress)
            logger.debug("Extracted road name: '{}'", roadName)
            logger.debug("Hash source string: '{}'", source)

            hash(source)
        } catch(e: Exception) {
            logger.error("Failed to generate id with name='$name', address='$address': ${e.message}")
            throw e
        }
    }

    private fun normalize(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }

    private fun extractRoadName(address: String): String {
        val roadRegex = Regex("""[가-힣]{2,}(로\d+번?길|로|길)""")
        return roadRegex.find(address)?.value ?: throw RuntimeException("cannot extract road name from $address")
    }

    private fun hash(input: String): String {
        return input.toByteArray(Charsets.UTF_8)
            .let { java.security.MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.simpleName)
    }
}