package com.tastecompass.baseutil.http

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object OkHttpHelper {

    private val gson = Gson()

    fun postRequest(url: String, body: String, authorization: String): JsonObject? {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = body.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $authorization")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: $response")
            }

            val responseBody = response.body?.string()
            return responseBody?.let {
                gson.fromJson(it, JsonObject::class.java)
            }
        }
    }
}