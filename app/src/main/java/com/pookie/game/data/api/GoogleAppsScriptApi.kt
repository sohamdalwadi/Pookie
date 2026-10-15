package com.pookie.game.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pookie.game.data.model.dto.ApiResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GoogleAppsScriptApi(
    private val baseUrlProvider: () -> String?
) {
    private val gson = Gson()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun resolveUrl(): String {
        val configured = baseUrlProvider()
        if (configured.isNullOrBlank()) {
            throw IllegalStateException("Backend deployment ID or URL is not configured.")
        }

        return if (configured.startsWith("http://") || configured.startsWith("https://")) {
            configured.trim()
        } else {
            // Treat as deployment ID
            "https://script.google.com/macros/s/${configured.trim()}/exec"
        }
    }

    suspend fun get(action: String, queryParams: Map<String, String> = emptyMap()): ApiResponseDto =
        withContext(Dispatchers.IO) {
            val base = resolveUrl()
            val urlBuilder = StringBuilder(base)
            urlBuilder.append(if (base.contains("?")) "&" else "?")
            urlBuilder.append("action=").append(action)

            for ((key, value) in queryParams) {
                urlBuilder.append("&").append(key).append("=").append(java.net.URLEncoder.encode(value, "UTF-8"))
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build()

            executeRequest(request)
        }

    suspend fun post(action: String, payload: Map<String, Any?>): ApiResponseDto =
        withContext(Dispatchers.IO) {
            val targetUrl = resolveUrl()

            val bodyJson = JsonObject().apply {
                addProperty("action", action)
                add("payload", gson.toJsonTree(payload))
            }

            val requestBody = bodyJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .build()

            executeRequest(request)
        }

    private fun executeRequest(request: Request): ApiResponseDto {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response from backend (HTTP ${response.code})")

            if (!response.isSuccessful && response.code !in 300..399) {
                throw IOException("Server returned error ${response.code}: $responseBody")
            }

            return try {
                gson.fromJson(responseBody, ApiResponseDto::class.java)
            } catch (e: Exception) {
                throw IOException("Failed to parse backend response: $responseBody", e)
            }
        }
    }
}
