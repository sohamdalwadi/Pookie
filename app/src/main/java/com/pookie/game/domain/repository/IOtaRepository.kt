package com.pookie.game.domain.repository

import com.pookie.game.domain.model.OtaUpdateInfo

interface IOtaRepository {
    suspend fun checkForUpdates(currentVersion: String): Result<OtaUpdateInfo>
}
