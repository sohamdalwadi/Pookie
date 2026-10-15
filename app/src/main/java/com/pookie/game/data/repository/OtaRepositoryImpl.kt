package com.pookie.game.data.repository

import com.google.gson.Gson
import com.pookie.game.data.model.dto.GitHubReleaseDto
import com.pookie.game.domain.model.OtaUpdateInfo
import com.pookie.game.domain.repository.IOtaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OtaRepositoryImpl : IOtaRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/sohamdalwadi/Pookie/releases/latest"
    }

    override suspend fun checkForUpdates(currentVersion: String): Result<OtaUpdateInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(GITHUB_LATEST_RELEASE_URL)
                    .header("User-Agent", "Pookie-Android-Client")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@runCatching OtaUpdateInfo(
                            hasUpdate = false,
                            latestVersion = currentVersion,
                            currentVersion = currentVersion,
                            downloadUrl = null,
                            releaseNotes = null
                        )
                    }

                    val body = response.body?.string() ?: return@runCatching OtaUpdateInfo(
                        hasUpdate = false,
                        latestVersion = currentVersion,
                        currentVersion = currentVersion,
                        downloadUrl = null,
                        releaseNotes = null
                    )

                    val release = gson.fromJson(body, GitHubReleaseDto::class.java)
                    val rawTag = release.tagName.removePrefix("v").trim()
                    val normalizedCurrent = currentVersion.removePrefix("v").trim()

                    val apkAsset = release.assets?.find { it.name.endsWith(".apk") }
                    val downloadUrl = apkAsset?.browserDownloadUrl
                        ?: "https://github.com/sohamdalwadi/Pookie/releases/latest"

                    val isNewer = compareSemver(rawTag, normalizedCurrent) > 0

                    OtaUpdateInfo(
                        hasUpdate = isNewer,
                        latestVersion = release.tagName,
                        currentVersion = currentVersion,
                        downloadUrl = downloadUrl,
                        releaseNotes = release.body
                    )
                }
            }
        }

    private fun compareSemver(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }
}
