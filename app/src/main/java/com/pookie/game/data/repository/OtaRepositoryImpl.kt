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

    override suspend fun checkForUpdates(currentVersion: String): Result<OtaUpdateInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Query branch-specific channel endpoint from BuildConfig (develop vs main)
                val targetEndpoint = com.pookie.game.BuildConfig.OTA_UPDATE_URL
                val release = fetchRelease(targetEndpoint)

                if (release == null) {
                    return@runCatching OtaUpdateInfo(
                        hasUpdate = false,
                        latestVersion = currentVersion,
                        currentVersion = currentVersion,
                        downloadUrl = null,
                        releaseNotes = null
                    )
                }

                val tagSemver = Regex("""\d+\.\d+\.\d+""").find(release.tagName)?.value
                    ?: Regex("""\d+\.\d+\.\d+""").find(release.name ?: "")?.value
                    ?: release.tagName.removePrefix("v").trim()
                val normalizedCurrent = currentVersion.removePrefix("v").trim()

                val apkAsset = release.assets?.find { it.name.endsWith(".apk") }
                val downloadUrl = apkAsset?.browserDownloadUrl
                    ?: "https://github.com/sohamdalwadi/Pookie/releases"

                val isNewer = compareSemver(tagSemver, normalizedCurrent) > 0

                OtaUpdateInfo(
                    hasUpdate = isNewer,
                    latestVersion = "v$tagSemver",
                    currentVersion = currentVersion,
                    downloadUrl = downloadUrl,
                    releaseNotes = release.body
                )
            }
        }

    private fun fetchRelease(url: String): GitHubReleaseDto? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Pookie-Android-Client")
            .get()
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            runCatching { gson.fromJson(body, GitHubReleaseDto::class.java) }.getOrNull()
        }
    }

    private fun fetchFirstReleaseFromList(url: String): GitHubReleaseDto? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Pookie-Android-Client")
            .get()
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val listType = object : com.google.gson.reflect.TypeToken<List<GitHubReleaseDto>>() {}.type
            val releases: List<GitHubReleaseDto>? = runCatching {
                gson.fromJson<List<GitHubReleaseDto>>(body, listType)
            }.getOrNull()
            releases?.firstOrNull()
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
