package com.pookie.game.domain.usecase

import com.pookie.game.domain.model.OtaUpdateInfo
import com.pookie.game.domain.repository.IOtaRepository

class CheckOtaUpdateUseCase(private val otaRepository: IOtaRepository) {
    suspend operator fun invoke(currentVersion: String): Result<OtaUpdateInfo> {
        return otaRepository.checkForUpdates(currentVersion)
    }
}
