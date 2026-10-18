package com.pookie.game.data.api

import android.util.Log
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

    companion object {
        private const val TAG = "PookieApi"

        fun sanitizeEndpoint(input: String): String {
            var cleaned = input.trim().replace("\"", "").replace("'", "")

            // If it's a Google Apps Script Web App URL, extract the deployment ID
            if (cleaned.contains("macros/s/")) {
                val cleanId = cleaned.substringAfter("macros/s/")
                    .substringBefore("/exec")
                    .substringBefore("/dev")
                    .substringBefore("?")
                    .substringBefore("/")
                    .trim()
                return "https://script.google.com/macros/s/$cleanId/exec"
            }
            
            // If other full URL (e.g. custom mock proxy or local dev server)
            if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
                if (cleaned.endsWith("/dev")) {
                    cleaned = cleaned.substringBeforeLast("/dev") + "/exec"
                } else if (!cleaned.endsWith("/exec")) {
                    cleaned = cleaned.trimEnd('/') + "/exec"
                }
                return cleaned
            }

            // If bare Deployment ID (e.g. AKfycb...)
            val cleanId = cleaned.substringBefore("?").substringBefore("/").trim()
            return "https://script.google.com/macros/s/$cleanId/exec"
        }
    }

    private fun resolveUrl(): String {
        val configured = baseUrlProvider()
        if (configured.isNullOrBlank()) {
            throw IllegalStateException("Backend deployment ID or URL is not configured.")
        }
        return sanitizeEndpoint(configured)
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

            val finalUrl = urlBuilder.toString()
            Log.d(TAG, "GET request: $finalUrl")

            val request = Request.Builder()
                .url(finalUrl)
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

            Log.d(TAG, "POST request: $targetUrl with action=$action")

            val requestBody = bodyJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(targetUrl)
                .post(requestBody)
                .build()

            executeRequest(request)
        }

    private fun executeRequest(request: Request): ApiResponseDto {
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            val code = response.code
            val finalUrl = response.request.url.toString()

            Log.d(TAG, "Response code=$code, finalUrl=$finalUrl")

            // Check if redirected to Google Accounts login (HTTP 401 or login redirect)
            if (code == 401 || finalUrl.contains("accounts.google.com/ServiceLogin") ||
                responseBody.contains("ServiceLogin") || responseBody.contains("Google Accounts")
            ) {
                throw IOException(
                    "Google Apps Script Error: Access Denied (401 Unauthorized).\n\n" +
                    "Reason: Your deployment is restricted to Google Accounts.\n\n" +
                    "Fix in 30 seconds:\n" +
                    "1. In your Google Sheet, open Extensions > Apps Script.\n" +
                    "2. Click Deploy > Manage deployments.\n" +
                    "3. Click the Edit (pencil) icon.\n" +
                    "4. Change 'Who has access' to 'Anyone' (NOT 'Only myself').\n" +
                    "5. Click Deploy and retry!"
                )
            }

            if (!response.isSuccessful && code !in 300..399) {
                throw IOException("Server returned error HTTP $code: $responseBody")
            }

            if (responseBody.isBlank()) {
                throw IOException("Empty response from backend (HTTP $code)")
            }

            return try {
                gson.fromJson(responseBody, ApiResponseDto::class.java)
            } catch (e: Exception) {
                throw IOException("Failed to parse backend response as JSON: $responseBody", e)
            }
        }
    }
}
