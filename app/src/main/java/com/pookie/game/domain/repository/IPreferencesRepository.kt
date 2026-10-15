package com.pookie.game.domain.repository

import com.pookie.game.domain.model.CoupleProfile

interface IPreferencesRepository {
    fun getBackendDeploymentId(): String?
    fun saveBackendDeploymentId(deploymentId: String)
    fun getBackendUrl(): String?
    fun saveBackendUrl(url: String)

    fun getCoupleProfile(): CoupleProfile
    fun saveCoupleProfile(profile: CoupleProfile)

    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)
}
