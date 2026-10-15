package com.pookie.game.domain.model

data class OtaUpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String?,
    val releaseNotes: String?
)
