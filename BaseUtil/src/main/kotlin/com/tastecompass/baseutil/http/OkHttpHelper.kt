package com.tastecompass.baseutil.http

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object OkHttpHelper {

    fun postRequest(url: String, body: String, authorization: String): JSONObject? {
        val client = OkHttpClient()
        val mediaType = "text/plain".toMediaTypeOrNull()
        val requestBody = body.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $authorization")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            response.body?.string()?.let { responseBody ->
                return JSONObject(responseBody)
            }
        }
        return null
    }

}