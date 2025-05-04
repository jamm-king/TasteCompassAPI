package com.tastecompass.controller.identifier

import com.tastecompass.analyzer.dto.AnalysisResult
import com.tastecompass.domain.entity.RestaurantProperty
import org.springframework.stereotype.Component
import java.text.Normalizer

@Component
class IdGenerator {

    fun generate(result: AnalysisResult): String {
        val name = result.name.trim()
        val roadName = extractRoadName(normalize(result.address ?: ""))
        val source = "$name|$roadName"

        return hash(source)
    }

    private fun normalize(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFC)
    }

    private fun extractRoadName(address: String): String {
        val roadRegex = Regex("""\b([가-힣]+로|[가-힣]+길)\b""")
        return roadRegex.find(address)?.value ?: RestaurantProperty.ADDRESS.defaultValue as String
    }

    private fun hash(input: String): String {
        return input.toByteArray(Charsets.UTF_8)
            .let { java.security.MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }
    }
}