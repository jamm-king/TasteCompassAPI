package com.tastecompass.kakao.client

import com.google.gson.Gson
import com.tastecompass.kakao.common.KakaoProperties
import com.tastecompass.kakao.dto.GeocodeResult
import com.tastecompass.kakao.dto.KakaoAddressResponse
import com.tastecompass.kakao.exception.AddressNormalizationException
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class KakaoMapClientImpl(
    private val kakaoProperties: KakaoProperties
) : KakaoMapClient {
    private val client = OkHttpClient()
    private val gson = Gson()

    override suspend fun geocode(rawAddress: String): GeocodeResult = coroutineScope {
        val encoded = URLEncoder.encode(rawAddress, "UTF-8")
        val url = "https://dapi.kakao.com/v2/local/search/address.json?query=$encoded"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "KakaoAK ${kakaoProperties.apiKey}")
            .get()
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw AddressNormalizationException("Kakao API call failed: HTTP ${resp.code}")
            }

            val body = resp.body?.string().orEmpty()
            if (body.isBlank()) {
                throw AddressNormalizationException("Kakao API response was empty for query '$rawAddress'")
            }

            val kakaoResp = try {
                gson.fromJson(body, KakaoAddressResponse::class.java)
            } catch (e: Exception) {
                throw AddressNormalizationException("Failed to parse Kakao API response")
            }

            val doc = kakaoResp.documents.firstOrNull()
                ?: throw AddressNormalizationException("No address document found for '$rawAddress'")

            val road = doc.roadAddress
                ?: throw AddressNormalizationException("No road-name address found for '$rawAddress'")
            val coords = doc.address
                ?: throw AddressNormalizationException("No coordinates found for '$rawAddress'")

            val x = coords.x.toDoubleOrNull()
                ?: throw AddressNormalizationException("Invalid x value '${coords.x}' for '$rawAddress'")
            val y = coords.y.toDoubleOrNull()
                ?: throw AddressNormalizationException("Invalid y value '${coords.y}' for '$rawAddress'")

            GeocodeResult(
                normalizedAddress = road.addressName,
                x = x,
                y = y
            )
        }
    }
}